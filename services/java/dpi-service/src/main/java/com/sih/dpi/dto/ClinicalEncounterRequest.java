package com.sih.dpi.dto;

import com.sih.dpi.entity.ClinicalEncounter.EncounterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalEncounterRequest {

    @NotNull(message = "L'identifiant de l'encounter GAP est obligatoire")
    private Long gapEncounterId;

    @NotBlank(message = "La référence patient est obligatoire")
    private String patientRef;

    private String empiGlobalUuid;

    @NotNull(message = "Le type d'encounter est obligatoire")
    private EncounterType encounterType;

    private String chiefComplaint;
    private String historyOfPresentIllness;
    private String pastMedicalHistory;
    private String allergies;
    private String currentMedications;
    private String attendingPhysicianName;
    private String attendingPhysicianId;
    private String specialty;
}
