package com.sih.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Événement: Résultat d'imagerie prêt
 * Criticité: MOYENNE - Artemis pour garantie, Kafka pour audit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResultReadyEvent extends DomainEvent {
    
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
