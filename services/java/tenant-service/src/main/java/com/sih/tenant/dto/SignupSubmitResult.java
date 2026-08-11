package com.sih.tenant.dto;

import com.sih.tenant.entity.SignupRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SignupSubmitResult {
    private String requestId;
    private SignupRequestStatus status;
    private boolean provisioned;
    private String tenantId;
    private String message;
    private String temporaryPassword;
    private LocalDateTime createdAt;
}
