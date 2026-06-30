package com.sih.dpi.dto;

import com.sih.dpi.entity.ClinicalEncounter.EncounterStatus;
import com.sih.dpi.entity.ClinicalEncounter.EncounterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalEncounterResponse {

    private Long id;
    private Long gapEncounterId;
    private String patientRef;
    private String empiGlobalUuid;

    private EncounterType encounterType;
    private EncounterStatus status;

    private String chiefComplaint;
    private String historyOfPresentIllness;
    private String pastMedicalHistory;
    private String allergies;
    private String currentMedications;
    private String physicalExamination;

    private String primaryDiagnosisCode;
    private String primaryDiagnosisLabel;
    private String secondaryDiagnosesCodes;

    private String treatmentPlan;
    private String clinicalSummary;

    private String attendingPhysicianName;
    private String attendingPhysicianId;
    private String specialty;

    private List<VitalSignResponse> vitalSigns;
    private List<MedicationOrderResponse> medicationOrders;
    private List<LabOrderResponse> labOrders;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
