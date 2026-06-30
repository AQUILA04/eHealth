package com.sih.dpi.dto;

import com.sih.dpi.entity.MedicationOrder.Route;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationOrderRequest {

    @NotNull
    private Long clinicalEncounterId;

    @NotBlank
    private String medicationName;

    private String genericName;
    private String atcCode;

    @NotBlank
    private String dose;

    @NotBlank
    private String unit;

    @NotNull
    private Route route;

    @NotBlank
    private String frequency;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer durationDays;
    private String instructions;
    private String indication;
    private String prescribedBy;
    private String prescribedById;
}
