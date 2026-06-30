package com.sih.dpi.dto;

import com.sih.dpi.entity.LabOrder.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabOrderResponse {

    private Long id;
    private Long clinicalEncounterId;

    private OrderType orderType;
    private String examName;
    private String examCode;
    private String indication;
    private String instructions;
    private Priority priority;
    private OrderStatus status;

    private String result;
    private String resultUnit;
    private String referenceRange;
    private ResultInterpretation interpretation;
    private LocalDateTime resultDate;
    private String resultComment;

    private String orderedBy;
    private String orderedById;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
