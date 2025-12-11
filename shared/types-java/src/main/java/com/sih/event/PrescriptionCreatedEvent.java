package com.sih.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Événement: Prescription créée
 * Criticité: TRÈS HAUTE - Transaction ACID requise (Artemis)
 * Audit: Immuable (Kafka)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionCreatedEvent extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("prescriptionId")
    private String prescriptionId;
    
    @JsonProperty("patientId")
    private String patientId;
    
    @JsonProperty("prescribingPhysicianId")
    private String prescribingPhysicianId;
    
    @JsonProperty("medicationCode")
    private String medicationCode;
    
    @JsonProperty("medicationName")
    private String medicationName;
    
    @JsonProperty("dosage")
    private String dosage;
    
    @JsonProperty("frequency")
    private String frequency;
    
    @JsonProperty("route")
    private String route;  // ORAL, IV, IM, SC, TOPICAL, etc.
    
    @JsonProperty("startDate")
    private LocalDateTime startDate;
    
    @JsonProperty("endDate")
    private LocalDateTime endDate;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("refills")
    private Integer refills;
    
    @JsonProperty("indication")
    private String indication;
    
    @JsonProperty("contraindications")
    private String[] contraindications;
    
    @JsonProperty("priority")
    private String priority;  // ROUTINE, URGENT, STAT
    
    @JsonProperty("notes")
    private String notes;
}
