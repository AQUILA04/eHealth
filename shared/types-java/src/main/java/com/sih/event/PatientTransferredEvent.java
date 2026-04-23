package com.sih.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Événement: Patient transféré
 * Criticité: HAUTE - Transaction ACID requise (Artemis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientTransferredEvent extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("patientId")
    private String patientId;
    
    @JsonProperty("fromDepartmentId")
    private String fromDepartmentId;
    
    @JsonProperty("toDepartmentId")
    private String toDepartmentId;
    
    @JsonProperty("fromBedNumber")
    private String fromBedNumber;
    
    @JsonProperty("toBedNumber")
    private String toBedNumber;
    
    @JsonProperty("transferReason")
    private String transferReason;
    
    @JsonProperty("transferDate")
    private LocalDateTime transferDate;
}
