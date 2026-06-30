package com.sih.dpi.dto;

import com.sih.dpi.entity.MedicationOrder.OrderStatus;
import com.sih.dpi.entity.MedicationOrder.Route;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationOrderResponse {

    private Long id;
    private Long clinicalEncounterId;

    private String medicationName;
    private String genericName;
    private String atcCode;
    private String dose;
    private String unit;
    private Route route;
    private String frequency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer durationDays;
    private OrderStatus status;
    private String instructions;
    private String indication;
    private String prescribedBy;
    private String prescribedById;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
