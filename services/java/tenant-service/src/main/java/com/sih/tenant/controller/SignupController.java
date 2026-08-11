package com.sih.tenant.controller;

import com.sih.tenant.dto.*;
import com.sih.tenant.entity.SignupRequestStatus;
import com.sih.tenant.service.SignupService;
import com.sih.tenant.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/signup")
@RequiredArgsConstructor
public class SignupController {

    private static final String SERVICE = "TENANT-SERVICE";

    private final SignupService signupService;
    private final SubscriptionPlanService planService;

    @GetMapping("/plans")
    public ResponseEntity<Response<List<PlanResponse>>> publicPlans() {
        return ResponseEntity.ok(Response.<List<PlanResponse>>builder()
                .status(HttpStatus.OK)
                .statusCode(200)
                .message("Plans publics")
                .service(SERVICE)
                .data(planService.listPublic())
                .build());
    }

    @PostMapping
    public ResponseEntity<Response<SignupSubmitResult>> submit(@Valid @RequestBody SignupSubmitRequest request) {
        SignupSubmitResult result = signupService.submit(request);
        HttpStatus status = result.isProvisioned() ? HttpStatus.CREATED : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(Response.<SignupSubmitResult>builder()
                .status(status)
                .statusCode(status.value())
                .message(result.getMessage())
                .service(SERVICE)
                .data(result)
                .build());
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_SYSTEM')")
    public ResponseEntity<Response<List<SignupRequestResponse>>> listRequests(
            @RequestParam(required = false) SignupRequestStatus status
    ) {
        return ResponseEntity.ok(Response.<List<SignupRequestResponse>>builder()
                .status(HttpStatus.OK)
                .statusCode(200)
                .message("Demandes d'inscription")
                .service(SERVICE)
                .data(signupService.listRequests(status))
                .build());
    }

    @PostMapping("/requests/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_SYSTEM')")
    public ResponseEntity<Response<SignupSubmitResult>> approve(
            @PathVariable String id,
            Authentication authentication
    ) {
        String reviewer = authentication != null ? authentication.getName() : "superadmin";
        SignupSubmitResult result = signupService.approve(id, reviewer);
        return ResponseEntity.ok(Response.<SignupSubmitResult>builder()
                .status(HttpStatus.OK)
                .statusCode(200)
                .message(result.getMessage())
                .service(SERVICE)
                .data(result)
                .build());
    }

    @PostMapping("/requests/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_SYSTEM')")
    public ResponseEntity<Response<SignupRequestResponse>> reject(
            @PathVariable String id,
            @RequestBody(required = false) RejectSignupRequest body,
            Authentication authentication
    ) {
        String reviewer = authentication != null ? authentication.getName() : "superadmin";
        String reason = body != null ? body.getReason() : null;
        SignupRequestResponse result = signupService.reject(id, reason, reviewer);
        return ResponseEntity.ok(Response.<SignupRequestResponse>builder()
                .status(HttpStatus.OK)
                .statusCode(200)
                .message("Demande rejetée")
                .service(SERVICE)
                .data(result)
                .build());
    }
}
