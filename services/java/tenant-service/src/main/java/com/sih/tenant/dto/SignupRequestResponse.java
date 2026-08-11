package com.sih.tenant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sih.tenant.entity.SignupRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SignupRequestResponse {
    private String id;
    private String organizationName;
    private String subdomain;
    private String adminEmail;
    private String adminFirstName;
    private String adminLastName;
    private String adminPhone;
    private String planId;
    private String planName;
    @JsonProperty("planFree")
    private boolean planFree;
    private SignupRequestStatus status;
    private String tenantId;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private String provisionError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
