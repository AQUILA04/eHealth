package com.sih.gap.dto;

import com.sih.gap.entity.Encounter.DischargeDisposition;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la gestion de la sortie d'un patient (Discharge).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DischargeRequest {

    @NotNull(message = "La disposition de sortie est obligatoire")
    private DischargeDisposition dischargeDisposition;

    private LocalDateTime dischargeDate;

    private String dischargeSummary;
}
