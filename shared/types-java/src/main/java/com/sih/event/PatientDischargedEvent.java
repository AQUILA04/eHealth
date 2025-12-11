package com.sih.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Événement: Patient sortie de l'hôpital
 * Criticité: HAUTE - Transaction ACID requise (Artemis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDischargedEvent extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("patientId")
    private String patientId;
    
    @JsonProperty("dischargeDate")
    private LocalDateTime dischargeDate;
    
    @JsonProperty("dischargeType")
    private String dischargeType;  // NORMAL, AGAINST_MEDICAL_ADVICE, TRANSFER, DEATH
    
    @JsonProperty("dischargeDestination")
    private String dischargeDestination;  // HOME, ANOTHER_HOSPITAL, NURSING_HOME, DEATH
    
    @JsonProperty("finalDiagnosisCodes")
    private String[] finalDiagnosisCodes;
    
    @JsonProperty("procedureCodes")
    private String[] procedureCodes;
    
    @JsonProperty("dischargingPhysicianId")
    private String dischargingPhysicianId;
    
    @JsonProperty("dischargeNotes")
    private String dischargeNotes;
}
