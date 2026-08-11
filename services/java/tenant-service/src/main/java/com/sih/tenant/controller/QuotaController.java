package com.sih.tenant.controller;

import com.sih.tenant.dto.QuotaAssertRequest;
import com.sih.tenant.dto.Response;
import com.sih.tenant.service.QuotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class QuotaController {

    private static final String SERVICE = "TENANT-SERVICE";

    private final QuotaService quotaService;

    @Value("${tenant.internal-api-key:ehealth-internal-dev-key}")
    private String internalApiKey;

    @PostMapping("/api/v1/internal/quota/assert")
    public ResponseEntity<Response<Void>> assertQuota(
            @RequestHeader(value = "X-Internal-Key", required = false) String key,
            @Valid @RequestBody QuotaAssertRequest request
    ) {
        if (internalApiKey != null && !internalApiKey.isBlank()
                && (key == null || !internalApiKey.equals(key))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Response.<Void>builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .statusCode(401)
                    .message("Invalid internal API key")
                    .service(SERVICE)
                    .build());
        }
        quotaService.assertWithinQuota(request);
        return ResponseEntity.ok(Response.<Void>builder()
                .status(HttpStatus.OK)
                .statusCode(200)
                .message("OK")
                .service(SERVICE)
                .build());
    }

    @GetMapping("/api/v1/subscriptions/me/usage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<Map<String, Object>>> myUsage(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId
    ) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("X-Tenant-ID requis");
        }
        return ResponseEntity.ok(Response.<Map<String, Object>>builder()
                .status(HttpStatus.OK)
                .statusCode(200)
                .message("Usage")
                .service(SERVICE)
                .data(quotaService.usageSummary(tenantId))
                .build());
    }
}
