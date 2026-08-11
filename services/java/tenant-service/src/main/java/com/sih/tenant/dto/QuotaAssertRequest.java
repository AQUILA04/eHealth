package com.sih.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuotaAssertRequest {
    @NotBlank
    private String tenantId;

    @NotBlank
    private String operation;

    /** For capacity checks: count after the intended create (current + 1). */
    private Long projectedCount;

    /** When true, increment usage counters after a successful usage assert. */
    private boolean recordUsage;

    private String idempotencyKey;
}
