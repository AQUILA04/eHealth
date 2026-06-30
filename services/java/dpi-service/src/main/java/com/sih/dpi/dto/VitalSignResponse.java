package com.sih.dpi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
}
