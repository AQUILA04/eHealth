package com.sih.dpi.service;

import com.sih.dpi.dto.*;
import com.sih.dpi.entity.ClinicalEncounter;
import com.sih.dpi.entity.ClinicalEncounter.EncounterStatus;
import com.sih.dpi.entity.LabOrder;
import com.sih.dpi.entity.MedicationOrder;
import com.sih.dpi.entity.VitalSign;
import com.sih.dpi.repository.ClinicalEncounterRepository;
import com.sih.dpi.repository.LabOrderRepository;
import com.sih.dpi.repository.MedicationOrderRepository;
import com.sih.dpi.repository.VitalSignRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service principal du DPI (Dossier Patient Informatisé — Module II).
 *
 * <p>Orchestre la gestion des dossiers cliniques, observations,
 * prescriptions et demandes d'examens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClinicalEncounterService {

    private final ClinicalEncounterRepository encounterRepo;
    private final VitalSignRepository vitalSignRepo;
    private final MedicationOrderRepository medicationRepo;
    private final LabOrderRepository labOrderRepo;

    // ─── ClinicalEncounter ────────────────────────────────────────────────────

    @Transactional
    public ClinicalEncounterResponse openEncounter(ClinicalEncounterRequest request) {
        log.info("DPI: Ouverture dossier clinique pour gapEncounterId={}", request.getGapEncounterId());

        ClinicalEncounter encounter = ClinicalEncounter.builder()
            .gapEncounterId(request.getGapEncounterId())
            .patientRef(request.getPatientRef())
            .empiGlobalUuid(request.getEmpiGlobalUuid())
            .encounterType(request.getEncounterType())
            .chiefComplaint(request.getChiefComplaint())
            .historyOfPresentIllness(request.getHistoryOfPresentIllness())
            .pastMedicalHistory(request.getPastMedicalHistory())
            .allergies(request.getAllergies())
            .currentMedications(request.getCurrentMedications())
            .attendingPhysicianName(request.getAttendingPhysicianName())
            .attendingPhysicianId(request.getAttendingPhysicianId())
            .specialty(request.getSpecialty())
            .build();

        return toResponse(encounterRepo.save(encounter));
    }

    public Optional<ClinicalEncounterResponse> findById(Long id) {
        return encounterRepo.findById(id).map(this::toResponse);
    }

    public Optional<ClinicalEncounterResponse> findByGapEncounterId(Long gapEncounterId) {
        return encounterRepo.findByGapEncounterId(gapEncounterId).map(this::toResponse);
    }

    public List<ClinicalEncounterResponse> findByPatient(String patientRef) {
        return encounterRepo.findByPatientRef(patientRef).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public Optional<ClinicalEncounterResponse> updateClinicalNotes(Long id, Map<String, String> updates) {
        return encounterRepo.findById(id).map(enc -> {
            if (updates.containsKey("chiefComplaint"))          enc.setChiefComplaint(updates.get("chiefComplaint"));
            if (updates.containsKey("historyOfPresentIllness")) enc.setHistoryOfPresentIllness(updates.get("historyOfPresentIllness"));
            if (updates.containsKey("physicalExamination"))     enc.setPhysicalExamination(updates.get("physicalExamination"));
            if (updates.containsKey("primaryDiagnosisCode"))    enc.setPrimaryDiagnosisCode(updates.get("primaryDiagnosisCode"));
            if (updates.containsKey("primaryDiagnosisLabel"))   enc.setPrimaryDiagnosisLabel(updates.get("primaryDiagnosisLabel"));
            if (updates.containsKey("secondaryDiagnosesCodes")) enc.setSecondaryDiagnosesCodes(updates.get("secondaryDiagnosesCodes"));
            if (updates.containsKey("treatmentPlan"))           enc.setTreatmentPlan(updates.get("treatmentPlan"));
            if (updates.containsKey("clinicalSummary"))         enc.setClinicalSummary(updates.get("clinicalSummary"));
            if (updates.containsKey("allergies"))               enc.setAllergies(updates.get("allergies"));
            return toResponse(encounterRepo.save(enc));
        });
    }

    @Transactional
    public Optional<ClinicalEncounterResponse> closeEncounter(Long id, String clinicalSummary) {
        return encounterRepo.findById(id).map(enc -> {
            enc.setStatus(EncounterStatus.FINISHED);
            if (clinicalSummary != null) enc.setClinicalSummary(clinicalSummary);
            log.info("DPI: Dossier clinique id={} clôturé", id);
            return toResponse(encounterRepo.save(enc));
        });
    }

    // ─── VitalSigns ───────────────────────────────────────────────────────────

    @Transactional
    public VitalSignResponse recordVitalSigns(VitalSignRequest request) {
        ClinicalEncounter encounter = encounterRepo.findById(request.getClinicalEncounterId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Encounter clinique introuvable: " + request.getClinicalEncounterId()));

        VitalSign vs = VitalSign.builder()
            .clinicalEncounter(encounter)
            .temperatureCelsius(request.getTemperatureCelsius())
            .heartRateBpm(request.getHeartRateBpm())
            .respiratoryRateCpm(request.getRespiratoryRateCpm())
            .bloodPressureSystolic(request.getBloodPressureSystolic())
            .bloodPressureDiastolic(request.getBloodPressureDiastolic())
            .oxygenSaturationPercent(request.getOxygenSaturationPercent())
            .bloodGlucoseMmolL(request.getBloodGlucoseMmolL())
            .weightKg(request.getWeightKg())
            .heightCm(request.getHeightCm())
            .painScore(request.getPainScore())
            .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now())
            .recordedBy(request.getRecordedBy())
            .notes(request.getNotes())
            .build();

        return toVsResponse(vitalSignRepo.save(vs));
    }

    public List<VitalSignResponse> getVitalSigns(Long encounterId) {
        return vitalSignRepo.findByClinicalEncounterIdOrderByRecordedAtDesc(encounterId).stream()
            .map(this::toVsResponse)
            .collect(Collectors.toList());
    }

    // ─── MedicationOrders ─────────────────────────────────────────────────────

    @Transactional
    public MedicationOrderResponse prescribeMedication(MedicationOrderRequest request) {
        ClinicalEncounter encounter = encounterRepo.findById(request.getClinicalEncounterId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Encounter clinique introuvable: " + request.getClinicalEncounterId()));

        MedicationOrder order = MedicationOrder.builder()
            .clinicalEncounter(encounter)
            .medicationName(request.getMedicationName())
            .genericName(request.getGenericName())
            .atcCode(request.getAtcCode())
            .dose(request.getDose())
            .unit(request.getUnit())
            .route(request.getRoute())
            .frequency(request.getFrequency())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .durationDays(request.getDurationDays())
            .instructions(request.getInstructions())
            .indication(request.getIndication())
            .prescribedBy(request.getPrescribedBy())
            .prescribedById(request.getPrescribedById())
            .build();

        return toMedResponse(medicationRepo.save(order));
    }

    public List<MedicationOrderResponse> getMedications(Long encounterId) {
        return medicationRepo.findByClinicalEncounterId(encounterId).stream()
            .map(this::toMedResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public Optional<MedicationOrderResponse> updateMedicationStatus(
            Long orderId, MedicationOrder.OrderStatus status) {
        return medicationRepo.findById(orderId).map(order -> {
            order.setStatus(status);
            return toMedResponse(medicationRepo.save(order));
        });
    }

    // ─── LabOrders ────────────────────────────────────────────────────────────

    @Transactional
    public LabOrderResponse orderExam(LabOrderRequest request) {
        ClinicalEncounter encounter = encounterRepo.findById(request.getClinicalEncounterId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Encounter clinique introuvable: " + request.getClinicalEncounterId()));

        LabOrder order = LabOrder.builder()
            .clinicalEncounter(encounter)
            .orderType(request.getOrderType())
            .examName(request.getExamName())
            .examCode(request.getExamCode())
            .indication(request.getIndication())
            .instructions(request.getInstructions())
            .priority(request.getPriority() != null ? request.getPriority() : LabOrder.Priority.ROUTINE)
            .orderedBy(request.getOrderedBy())
            .orderedById(request.getOrderedById())
            .build();

        return toLabResponse(labOrderRepo.save(order));
    }

    public List<LabOrderResponse> getLabOrders(Long encounterId) {
        return labOrderRepo.findByClinicalEncounterId(encounterId).stream()
            .map(this::toLabResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public Optional<LabOrderResponse> recordLabResult(Long orderId, LabResultRequest request) {
        return labOrderRepo.findById(orderId).map(order -> {
            order.setResult(request.getResult());
            order.setResultUnit(request.getResultUnit());
            order.setReferenceRange(request.getReferenceRange());
            order.setInterpretation(request.getInterpretation());
            order.setResultDate(request.getResultDate() != null
                ? request.getResultDate() : LocalDateTime.now());
            order.setResultComment(request.getResultComment());
            order.setStatus(LabOrder.OrderStatus.COMPLETED);
            log.info("DPI: Résultat enregistré pour labOrder id={}", orderId);
            return toLabResponse(labOrderRepo.save(order));
        });
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private ClinicalEncounterResponse toResponse(ClinicalEncounter e) {
        List<VitalSignResponse> vs = e.getVitalSigns().stream()
            .map(this::toVsResponse).collect(Collectors.toList());
        List<MedicationOrderResponse> meds = e.getMedicationOrders().stream()
            .map(this::toMedResponse).collect(Collectors.toList());
        List<LabOrderResponse> labs = e.getLabOrders().stream()
            .map(this::toLabResponse).collect(Collectors.toList());

        return ClinicalEncounterResponse.builder()
            .id(e.getId())
            .gapEncounterId(e.getGapEncounterId())
            .patientRef(e.getPatientRef())
            .empiGlobalUuid(e.getEmpiGlobalUuid())
            .encounterType(e.getEncounterType())
            .status(e.getStatus())
            .chiefComplaint(e.getChiefComplaint())
            .historyOfPresentIllness(e.getHistoryOfPresentIllness())
            .pastMedicalHistory(e.getPastMedicalHistory())
            .allergies(e.getAllergies())
            .currentMedications(e.getCurrentMedications())
            .physicalExamination(e.getPhysicalExamination())
            .primaryDiagnosisCode(e.getPrimaryDiagnosisCode())
            .primaryDiagnosisLabel(e.getPrimaryDiagnosisLabel())
            .secondaryDiagnosesCodes(e.getSecondaryDiagnosesCodes())
            .treatmentPlan(e.getTreatmentPlan())
            .clinicalSummary(e.getClinicalSummary())
            .attendingPhysicianName(e.getAttendingPhysicianName())
            .attendingPhysicianId(e.getAttendingPhysicianId())
            .specialty(e.getSpecialty())
            .vitalSigns(vs)
            .medicationOrders(meds)
            .labOrders(labs)
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }

    private VitalSignResponse toVsResponse(VitalSign v) {
        return VitalSignResponse.builder()
            .id(v.getId())
            .clinicalEncounterId(v.getClinicalEncounter().getId())
            .temperatureCelsius(v.getTemperatureCelsius())
            .heartRateBpm(v.getHeartRateBpm())
            .respiratoryRateCpm(v.getRespiratoryRateCpm())
            .bloodPressureSystolic(v.getBloodPressureSystolic())
            .bloodPressureDiastolic(v.getBloodPressureDiastolic())
            .oxygenSaturationPercent(v.getOxygenSaturationPercent())
            .bloodGlucoseMmolL(v.getBloodGlucoseMmolL())
            .weightKg(v.getWeightKg())
            .heightCm(v.getHeightCm())
            .bmi(v.getBmi())
            .painScore(v.getPainScore())
            .recordedAt(v.getRecordedAt())
            .recordedBy(v.getRecordedBy())
            .notes(v.getNotes())
            .criticalAlerts(evaluateVitalAlerts(v))
            .createdAt(v.getCreatedAt())
            .build();
    }

    /** Seuils de sécurité configurés pour une première alerte clinique visible dans le DPI. */
    private List<String> evaluateVitalAlerts(VitalSign v) {
        List<String> alerts = new java.util.ArrayList<>();
        if (v.getOxygenSaturationPercent() != null && v.getOxygenSaturationPercent().compareTo(new java.math.BigDecimal("90")) < 0) alerts.add("Saturation en oxygène critique (< 90 %)");
        if (v.getHeartRateBpm() != null && (v.getHeartRateBpm() < 40 || v.getHeartRateBpm() > 130)) alerts.add("Fréquence cardiaque critique");
        if (v.getRespiratoryRateCpm() != null && (v.getRespiratoryRateCpm() < 8 || v.getRespiratoryRateCpm() > 30)) alerts.add("Fréquence respiratoire critique");
        if (v.getBloodPressureSystolic() != null && (v.getBloodPressureSystolic() < 90 || v.getBloodPressureSystolic() > 180)) alerts.add("Pression artérielle systolique critique");
        if (v.getTemperatureCelsius() != null && v.getTemperatureCelsius().compareTo(new java.math.BigDecimal("39.5")) >= 0) alerts.add("Hyperthermie critique");
        if (v.getPainScore() != null && v.getPainScore() >= 8) alerts.add("Douleur sévère");
        return alerts;
    }

    private MedicationOrderResponse toMedResponse(MedicationOrder m) {
        return MedicationOrderResponse.builder()
            .id(m.getId())
            .clinicalEncounterId(m.getClinicalEncounter().getId())
            .medicationName(m.getMedicationName())
            .genericName(m.getGenericName())
            .atcCode(m.getAtcCode())
            .dose(m.getDose())
            .unit(m.getUnit())
            .route(m.getRoute())
            .frequency(m.getFrequency())
            .startDate(m.getStartDate())
            .endDate(m.getEndDate())
            .durationDays(m.getDurationDays())
            .status(m.getStatus())
            .instructions(m.getInstructions())
            .indication(m.getIndication())
            .prescribedBy(m.getPrescribedBy())
            .prescribedById(m.getPrescribedById())
            .createdAt(m.getCreatedAt())
            .updatedAt(m.getUpdatedAt())
            .build();
    }

    private LabOrderResponse toLabResponse(LabOrder l) {
        return LabOrderResponse.builder()
            .id(l.getId())
            .clinicalEncounterId(l.getClinicalEncounter().getId())
            .orderType(l.getOrderType())
            .examName(l.getExamName())
            .examCode(l.getExamCode())
            .indication(l.getIndication())
            .instructions(l.getInstructions())
            .priority(l.getPriority())
            .status(l.getStatus())
            .result(l.getResult())
            .resultUnit(l.getResultUnit())
            .referenceRange(l.getReferenceRange())
            .interpretation(l.getInterpretation())
            .resultDate(l.getResultDate())
            .resultComment(l.getResultComment())
            .orderedBy(l.getOrderedBy())
            .orderedById(l.getOrderedById())
            .createdAt(l.getCreatedAt())
            .updatedAt(l.getUpdatedAt())
            .build();
    }
}
