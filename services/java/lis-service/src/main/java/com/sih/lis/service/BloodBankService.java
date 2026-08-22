package com.sih.lis.service;

import com.sih.lis.dto.BloodBankDtos.*;
import com.sih.lis.entity.BloodUnit;
import com.sih.lis.entity.TransfusionRequest;
import com.sih.lis.repository.BloodUnitRepository;
import com.sih.lis.repository.TransfusionRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional(readOnly = true)
public class BloodBankService {
    private final BloodUnitRepository bloodUnitRepository;
    private final TransfusionRequestRepository transfusionRepository;

    @Transactional public BloodUnitResponse receive(ReceiveBloodUnitRequest request) {
        var unit = BloodUnit.builder().donationCode(request.donationCode()).aboGroup(request.aboGroup()).rhesus(request.rhesus()).component(request.component()).collectedOn(request.collectedOn()).expiresOn(request.expiresOn()).status(BloodUnit.Status.AVAILABLE).storageLocation(request.storageLocation()).build();
        return toUnitResponse(bloodUnitRepository.save(unit));
    }
    public List<BloodUnitResponse> listUnits(BloodUnit.Status status) {
        var units = status == null ? bloodUnitRepository.findAll() : bloodUnitRepository.findByStatusOrderByExpiresOnAsc(status);
        return units.stream().map(this::toUnitResponse).toList();
    }
    @Transactional public TransfusionResponse request(CreateTransfusionRequest request) {
        var compatibleUnit = bloodUnitRepository.findByStatusAndComponentAndExpiresOnGreaterThanEqualOrderByExpiresOnAsc(BloodUnit.Status.AVAILABLE, request.component(), LocalDate.now()).stream().filter(unit -> isCompatible(unit, request.recipientAboGroup(), request.recipientRhesus())).findFirst().orElseThrow(() -> new IllegalStateException("Aucune poche compatible et disponible n'a été trouvée."));
        compatibleUnit.setStatus(BloodUnit.Status.RESERVED);
        var transfusion = TransfusionRequest.builder().clinicalEncounterId(request.clinicalEncounterId()).patientRef(request.patientRef()).recipientAboGroup(request.recipientAboGroup()).recipientRhesus(request.recipientRhesus()).component(request.component()).bloodUnit(compatibleUnit).status(TransfusionRequest.Status.REQUESTED).requestedBy(request.requestedBy()).requestedAt(LocalDateTime.now()).build();
        bloodUnitRepository.save(compatibleUnit);
        return toTransfusionResponse(transfusionRepository.save(transfusion));
    }
    public List<TransfusionResponse> listTransfusions(TransfusionRequest.Status status, String patientRef) {
        var items = status != null ? transfusionRepository.findByStatusOrderByRequestedAtAsc(status) : patientRef != null && !patientRef.isBlank() ? transfusionRepository.findByPatientRefOrderByRequestedAtDesc(patientRef) : transfusionRepository.findAll();
        return items.stream().map(this::toTransfusionResponse).toList();
    }
    @Transactional public TransfusionResponse validateCrossmatch(Long id, CrossmatchRequest request) {
        var transfusion = find(id); requireStatus(transfusion, TransfusionRequest.Status.REQUESTED);
        if (!isCompatible(transfusion.getBloodUnit(), transfusion.getRecipientAboGroup(), transfusion.getRecipientRhesus())) throw new IllegalStateException("Compatibilité ABO/Rh non valide : délivrance verrouillée.");
        transfusion.setStatus(TransfusionRequest.Status.COMPATIBILITY_VALIDATED); transfusion.setCrossmatchValidatedBy(request.validatedBy()); transfusion.setCrossmatchValidatedAt(LocalDateTime.now());
        return toTransfusionResponse(transfusionRepository.save(transfusion));
    }
    @Transactional public TransfusionResponse issue(Long id, IssueRequest request) {
        var transfusion = find(id); requireStatus(transfusion, TransfusionRequest.Status.COMPATIBILITY_VALIDATED);
        transfusion.setStatus(TransfusionRequest.Status.ISSUED); transfusion.setIssuedBy(request.issuedBy()); transfusion.setIssuedAt(LocalDateTime.now()); transfusion.getBloodUnit().setStatus(BloodUnit.Status.ISSUED);
        bloodUnitRepository.save(transfusion.getBloodUnit()); return toTransfusionResponse(transfusionRepository.save(transfusion));
    }
    @Transactional public TransfusionResponse complete(Long id, CompleteTransfusionRequest request) {
        var transfusion = find(id); requireStatus(transfusion, TransfusionRequest.Status.ISSUED);
        transfusion.setStatus(TransfusionRequest.Status.COMPLETED); transfusion.setCompletedBy(request.completedBy()); transfusion.setCompletedAt(LocalDateTime.now()); transfusion.getBloodUnit().setStatus(BloodUnit.Status.TRANSFUSED);
        bloodUnitRepository.save(transfusion.getBloodUnit()); return toTransfusionResponse(transfusionRepository.save(transfusion));
    }
    @Transactional public TransfusionResponse reportReaction(Long id, ReportReactionRequest request) {
        var transfusion = find(id); if (transfusion.getStatus() != TransfusionRequest.Status.ISSUED && transfusion.getStatus() != TransfusionRequest.Status.COMPLETED) throw new IllegalStateException("Un incident ne peut être déclaré qu'après délivrance.");
        transfusion.setStatus(TransfusionRequest.Status.REACTION_REPORTED); transfusion.setReactionDescription(request.reactionDescription()); transfusion.setReactionReportedAt(LocalDateTime.now()); log.warn("Banque de sang: incident transfusionnel déclaré pour demande {}", id);
        return toTransfusionResponse(transfusionRepository.save(transfusion));
    }
    private TransfusionRequest find(Long id) { return transfusionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Demande transfusionnelle introuvable: " + id)); }
    private void requireStatus(TransfusionRequest item, TransfusionRequest.Status expected) { if (item.getStatus() != expected) throw new IllegalStateException("Transition invalide depuis le statut " + item.getStatus()); }
    private boolean isCompatible(BloodUnit unit, BloodUnit.AboGroup recipient, BloodUnit.Rhesus recipientRh) {
        boolean abo = switch (recipient) { case O -> unit.getAboGroup() == BloodUnit.AboGroup.O; case A -> unit.getAboGroup() == BloodUnit.AboGroup.A || unit.getAboGroup() == BloodUnit.AboGroup.O; case B -> unit.getAboGroup() == BloodUnit.AboGroup.B || unit.getAboGroup() == BloodUnit.AboGroup.O; case AB -> true; };
        boolean rhesus = recipientRh == BloodUnit.Rhesus.POSITIVE || unit.getRhesus() == BloodUnit.Rhesus.NEGATIVE;
        return abo && rhesus;
    }
    private BloodUnitResponse toUnitResponse(BloodUnit unit) { return new BloodUnitResponse(unit.getId(), unit.getDonationCode(), unit.getAboGroup(), unit.getRhesus(), unit.getComponent(), unit.getCollectedOn(), unit.getExpiresOn(), unit.getStatus(), unit.getStorageLocation(), !unit.getExpiresOn().isAfter(LocalDate.now().plusDays(14))); }
    private TransfusionResponse toTransfusionResponse(TransfusionRequest item) { var unit = item.getBloodUnit(); return new TransfusionResponse(item.getId(), item.getClinicalEncounterId(), item.getPatientRef(), item.getRecipientAboGroup(), item.getRecipientRhesus(), item.getComponent(), unit.getId(), unit.getDonationCode(), unit.getAboGroup(), unit.getRhesus(), item.getStatus(), item.getRequestedBy(), item.getCrossmatchValidatedBy(), item.getIssuedBy(), item.getCompletedBy(), item.getRequestedAt(), item.getCrossmatchValidatedAt(), item.getIssuedAt(), item.getCompletedAt(), item.getReactionDescription(), item.getReactionReportedAt()); }
}
