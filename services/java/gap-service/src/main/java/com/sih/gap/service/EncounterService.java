package com.sih.gap.service;

import com.sih.gap.dto.DischargeRequest;
import com.sih.gap.dto.EncounterRequest;
import com.sih.gap.dto.EncounterResponse;
import com.sih.gap.entity.Encounter;
import com.sih.gap.entity.Encounter.BedStatus;
import com.sih.gap.entity.Encounter.EncounterStatus;
import com.sih.gap.entity.Patient;
import com.sih.gap.repository.EncounterRepository;
import com.sih.gap.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service ADT (Admission, Discharge, Transfer) — Module I, Section 2.3.
 *
 * <p>Gère le cycle de vie complet d'un épisode de soins :
 * admission → transfert → sortie.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;
    private final com.sih.gap.client.QuotaClient quotaClient;

    // ─── Admission ────────────────────────────────────────────────────────────

    @Transactional
    public EncounterResponse admit(EncounterRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Patient introuvable avec l'id: " + request.getPatientId()));

        if (quotaClient != null) {
            quotaClient.assertAndRecordUsage("encounters.create");
        }

        if (request.getEncounterType() == Encounter.EncounterType.INPATIENT
            && request.getWard() != null && request.getRoom() != null && request.getBedNumber() != null
            && encounterRepository.existsByWardAndRoomAndBedNumberAndStatus(
                request.getWard(), request.getRoom(), request.getBedNumber(), EncounterStatus.IN_PROGRESS)) {
            throw new IllegalStateException("Le lit demandé est déjà occupé");
        }

        Encounter encounter = Encounter.builder()
            .patient(patient)
            .encounterType(request.getEncounterType())
            .admissionType(request.getAdmissionType() != null
                ? request.getAdmissionType() : Encounter.AdmissionType.SCHEDULED)
            .admissionDate(request.getAdmissionDate() != null
                ? request.getAdmissionDate() : LocalDateTime.now())
            .admissionReason(request.getAdmissionReason())
            .ward(request.getWard())
            .room(request.getRoom())
            .bedNumber(request.getBedNumber())
            .bedStatus(request.getEncounterType() == Encounter.EncounterType.INPATIENT ? BedStatus.OCCUPIED : null)
            .attendingPhysicianName(request.getAttendingPhysicianName())
            .attendingPhysicianId(request.getAttendingPhysicianId())
            .financialCoverage(request.getFinancialCoverage() != null
                ? request.getFinancialCoverage() : patient.getFinancialCoverage())
            .insurancePolicyNumber(request.getInsurancePolicyNumber() != null
                ? request.getInsurancePolicyNumber() : patient.getInsurancePolicyNumber())
            .status(EncounterStatus.IN_PROGRESS)
            .build();

        Encounter saved = encounterRepository.save(encounter);
        log.info("GAP/ADT: Admission — encounterId={}, patient={}, ward={}",
            saved.getId(), patient.getLocalMrn(), saved.getWard());
        return toResponse(saved);
    }

    // ─── Transfert ────────────────────────────────────────────────────────────

    @Transactional
    public Optional<EncounterResponse> transfer(Long encounterId, String newWard,
                                                  String newRoom, String newBed) {
        return encounterRepository.findById(encounterId).map(encounter -> {
            if (encounterRepository.existsByWardAndRoomAndBedNumberAndStatus(
                newWard, newRoom, newBed, EncounterStatus.IN_PROGRESS)
                && !(newWard.equals(encounter.getWard()) && newRoom.equals(encounter.getRoom()) && newBed.equals(encounter.getBedNumber()))) {
                throw new IllegalStateException("Le lit de destination est déjà occupé");
            }
            String oldWard = encounter.getWard();
            encounter.setWard(newWard);
            encounter.setRoom(newRoom);
            encounter.setBedNumber(newBed);
            encounter.setBedStatus(BedStatus.OCCUPIED);
            log.info("GAP/ADT: Transfert — encounterId={} de {} vers {}", encounterId, oldWard, newWard);
            return toResponse(encounterRepository.save(encounter));
        });
    }

    // ─── Sortie ───────────────────────────────────────────────────────────────

    @Transactional
    public Optional<EncounterResponse> discharge(Long encounterId, DischargeRequest request) {
        return encounterRepository.findById(encounterId).map(encounter -> {
            encounter.setStatus(EncounterStatus.FINISHED);
            encounter.setDischargeDate(request.getDischargeDate() != null
                ? request.getDischargeDate() : LocalDateTime.now());
            encounter.setDischargeDisposition(request.getDischargeDisposition());
            encounter.setDischargeSummary(request.getDischargeSummary());
            encounter.setBedStatus(BedStatus.CLEANING); // Déclenche le workflow de nettoyage
            log.info("GAP/ADT: Sortie — encounterId={}, disposition={}",
                encounterId, request.getDischargeDisposition());
            return toResponse(encounterRepository.save(encounter));
        });
    }

    /** Valide la fin du bio-nettoyage et rend le lit de nouveau disponible. */
    @Transactional
    public Optional<EncounterResponse> completeBedCleaning(Long encounterId) {
        return encounterRepository.findById(encounterId).map(encounter -> {
            if (encounter.getStatus() != EncounterStatus.FINISHED || encounter.getBedStatus() != BedStatus.CLEANING) {
                throw new IllegalStateException("Le lit ne peut pas être libéré avant la sortie et le nettoyage");
            }
            encounter.setBedStatus(BedStatus.AVAILABLE);
            log.info("GAP/ADT: Bio-nettoyage validé — encounterId={}, lit={}", encounterId, encounter.getBedNumber());
            return toResponse(encounterRepository.save(encounter));
        });
    }

    // ─── Lecture ──────────────────────────────────────────────────────────────

    public List<EncounterResponse> findByPatient(Long patientId) {
        return encounterRepository.findByPatientId(patientId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public Optional<EncounterResponse> findById(Long id) {
        return encounterRepository.findById(id).map(this::toResponse);
    }

    /** Tableau de bord des lits — tous les patients actuellement hospitalisés. */
    public List<EncounterResponse> getBedBoard() {
        return encounterRepository.findByStatus(EncounterStatus.IN_PROGRESS).stream()
            .filter(e -> e.getEncounterType() == Encounter.EncounterType.INPATIENT)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<EncounterResponse> findByWard(String ward) {
        return encounterRepository.findByWardAndStatus(ward, EncounterStatus.IN_PROGRESS).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private EncounterResponse toResponse(Encounter e) {
        return EncounterResponse.builder()
            .id(e.getId())
            .patientId(e.getPatient().getId())
            .patientFullName(e.getPatient().getFullName())
            .patientMrn(e.getPatient().getLocalMrn())
            .encounterType(e.getEncounterType())
            .status(e.getStatus())
            .admissionType(e.getAdmissionType())
            .admissionDate(e.getAdmissionDate())
            .admissionReason(e.getAdmissionReason())
            .ward(e.getWard())
            .room(e.getRoom())
            .bedNumber(e.getBedNumber())
            .bedStatus(e.getBedStatus())
            .attendingPhysicianName(e.getAttendingPhysicianName())
            .attendingPhysicianId(e.getAttendingPhysicianId())
            .dischargeDate(e.getDischargeDate())
            .dischargeDisposition(e.getDischargeDisposition())
            .dischargeSummary(e.getDischargeSummary())
            .financialCoverage(e.getFinancialCoverage())
            .insurancePolicyNumber(e.getInsurancePolicyNumber())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }
}
