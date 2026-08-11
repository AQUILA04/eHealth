package com.sih.tenant.dto;

import com.sih.tenant.entity.TenantStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    private String id;
    private String name;
    private String domain;
    private TenantStatus status;
    private String contactEmail;
    private String contactPhone;
    private String planId;
    private String planName;
    private String subscriptionStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
