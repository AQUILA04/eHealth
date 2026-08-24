package com.sih.dpi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignResponse {

    private Long id;
    private Long clinicalEncounterId;

    private BigDecimal temperatureCelsius;
    private Integer heartRateBpm;
    private Integer respiratoryRateCpm;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private BigDecimal oxygenSaturationPercent;
    private BigDecimal bloodGlucoseMmolL;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal bmi;
    private Integer painScore;

    private LocalDateTime recordedAt;
    private String recordedBy;
    private String notes;
    /** Alertes calculées à partir de seuils de sécurité, non persistées. */
    private List<String> criticalAlerts;
    private LocalDateTime createdAt;
}
