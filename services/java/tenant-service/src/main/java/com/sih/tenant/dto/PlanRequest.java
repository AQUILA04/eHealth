package com.sih.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PlanRequest {
    @NotBlank
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String billingInterval;
    private Boolean isPublic;
    private Boolean isActive;
    private Boolean isFree;
    private Boolean autoApproveSignups;
    private String stripePriceId;
    private Integer sortOrder;
    private Map<String, Object> limits;
    private Map<String, Boolean> features;
}
