package com.sih.dpi.dto;

import com.sih.dpi.entity.LabOrder.OrderType;
import com.sih.dpi.entity.LabOrder.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabOrderRequest {

    @NotNull
    private Long clinicalEncounterId;

    @NotNull
    private OrderType orderType;

    @NotBlank
    private String examName;

    private String examCode;
    private String indication;
    private String instructions;
    private Priority priority;
    private String orderedBy;
    private String orderedById;
}
