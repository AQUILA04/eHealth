package com.sih.dpi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class VitalSignRequest {

    @NotNull(message = "L'identifiant de l'encounter clinique est obligatoire")
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

    @Min(0) @Max(10)
    private Integer painScore;

    private LocalDateTime recordedAt;
    private String recordedBy;
    private String notes;
}
