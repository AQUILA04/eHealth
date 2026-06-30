package com.sih.gap.dto;

import com.sih.gap.entity.Encounter.*;
import com.sih.gap.entity.Patient.FinancialCoverage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncounterResponse {

    private Long id;
    private Long patientId;
    private String patientFullName;
    private String patientMrn;

    private EncounterType encounterType;
    private EncounterStatus status;
    private AdmissionType admissionType;
    private LocalDateTime admissionDate;
    private String admissionReason;

    private String ward;
    private String room;
    private String bedNumber;
    private BedStatus bedStatus;

    private String attendingPhysicianName;
    private String attendingPhysicianId;

    private LocalDateTime dischargeDate;
    private DischargeDisposition dischargeDisposition;
    private String dischargeSummary;

    private FinancialCoverage financialCoverage;
    private String insurancePolicyNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
