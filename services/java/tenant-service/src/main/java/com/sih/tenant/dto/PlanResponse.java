package com.sih.tenant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sih.tenant.entity.BillingInterval;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class PlanResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private BillingInterval billingInterval;
    @JsonProperty("isPublic")
    private boolean isPublic;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("isFree")
    private boolean isFree;
    private boolean autoApproveSignups;
    private String stripePriceId;
    private int sortOrder;
    private Map<String, Object> limits;
    private Map<String, Boolean> features;
    private LocalDateTime createdAt;
}
