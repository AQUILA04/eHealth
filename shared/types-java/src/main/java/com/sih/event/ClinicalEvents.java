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

/**
 * Événement: Résultat de laboratoire prêt
 * Criticité: MOYENNE - Artemis pour garantie, Kafka pour audit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class LabResultReadyEvent extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("labOrderId")
    private String labOrderId;
    
    @JsonProperty("patientId")
    private String patientId;
    
    @JsonProperty("testCode")
    private String testCode;
    
    @JsonProperty("testName")
    private String testName;
    
    @JsonProperty("result")
    private String result;
    
    @JsonProperty("resultUnit")
    private String resultUnit;
    
    @JsonProperty("referenceRange")
    private String referenceRange;
    
    @JsonProperty("abnormal")
    private Boolean abnormal;
    
    @JsonProperty("resultDate")
    private LocalDateTime resultDate;
    
    @JsonProperty("labId")
    private String labId;
    
    @JsonProperty("techniciandId")
    private String technicianId;
}

/**
 * Événement: Résultat d'imagerie prêt
 * Criticité: MOYENNE - Artemis pour garantie, Kafka pour audit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ImageResultReadyEvent extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("imageOrderId")
    private String imageOrderId;
    
    @JsonProperty("patientId")
    private String patientId;
    
    @JsonProperty("modalityCode")
    private String modalityCode;  // CT, MRI, XR, US, etc.
    
    @JsonProperty("modalityName")
    private String modalityName;
    
    @JsonProperty("bodyPartExamined")
    private String bodyPartExamined;
    
    @JsonProperty("studyDate")
    private LocalDateTime studyDate;
    
    @JsonProperty("imageCount")
    private Integer imageCount;
    
    @JsonProperty("dicomStudyId")
    private String dicomStudyId;
    
    @JsonProperty("radiologistId")
    private String radiologistId;
    
    @JsonProperty("reportStatus")
    private String reportStatus;  // PRELIMINARY, FINAL
    
    @JsonProperty("reportUrl")
    private String reportUrl;
}
