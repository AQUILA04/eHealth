package com.sih.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Événement: Résultat de laboratoire prêt
 * Criticité: MOYENNE - Artemis pour garantie, Kafka pour audit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResultReadyEvent extends DomainEvent {
    
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
