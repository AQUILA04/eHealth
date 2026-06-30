package com.sih.gap.dto;

import com.sih.gap.entity.Encounter.AdmissionType;
import com.sih.gap.entity.Encounter.EncounterType;
import com.sih.gap.entity.Patient.FinancialCoverage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncounterRequest {

    @NotNull(message = "L'identifiant du patient est obligatoire")
    private Long patientId;

    @NotNull(message = "Le type d'encounter est obligatoire")
    private EncounterType encounterType;

    private AdmissionType admissionType;
    private LocalDateTime admissionDate;
    private String admissionReason;

    private String ward;
    private String room;
    private String bedNumber;

    private String attendingPhysicianName;
    private String attendingPhysicianId;

    private FinancialCoverage financialCoverage;
    private String insurancePolicyNumber;
}
