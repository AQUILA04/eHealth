package com.sih.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Événement: Patient admis à l'hôpital
 * Criticité: HAUTE - Transaction ACID requise (Artemis)
 * Audit: Immuable (Kafka)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientAdmittedEvent extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("patientId")
    private String patientId;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("dateOfBirth")
    private String dateOfBirth;
    
    @JsonProperty("admissionDate")
    private LocalDateTime admissionDate;
    
    @JsonProperty("departmentId")
    private String departmentId;
    
    @JsonProperty("departmentName")
    private String departmentName;
    
    @JsonProperty("bedNumber")
    private String bedNumber;
    
    @JsonProperty("admissionType")
    private String admissionType;  // EMERGENCY, PLANNED, TRANSFER
    
    @JsonProperty("diagnosisCodes")
    private String[] diagnosisCodes;
    
    @JsonProperty("admittingPhysicianId")
    private String admittingPhysicianId;
    
    @JsonProperty("insuranceId")
    private String insuranceId;
    
    public PatientAdmittedEvent(String patientId, String firstName, String lastName) {
        super();
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.setEventType("PATIENT_ADMITTED");
    }
}
