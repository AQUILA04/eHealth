package com.sih.tenant.controller;

import com.sih.tenant.dto.*;
import com.sih.tenant.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions/plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private static final String SERVICE = "TENANT-SERVICE";
    private final SubscriptionPlanService planService;

    @GetMapping("/public")
    public ResponseEntity<Response<List<PlanResponse>>> listPublic() {
        return ok(planService.listPublic(), "Plans publics");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_SYSTEM')")
    public ResponseEntity<Response<List<PlanResponse>>> listAll() {
        return ok(planService.listAll(), "Tous les plans");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_SYSTEM')")
    public ResponseEntity<Response<PlanResponse>> get(@PathVariable String id) {
        return ok(planService.getById(id), "Plan");
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_SYSTEM')")
    public ResponseEntity<Response<PlanResponse>> create(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wrap(planService.create(request), "Plan créé", HttpStatus.CREATED));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_SYSTEM')")
    public ResponseEntity<Response<PlanResponse>> update(
            @PathVariable String id,
            @RequestBody PlanRequest request
    ) {
        return ok(planService.update(id, request), "Plan mis à jour");
    }

    private ResponseEntity<Response<List<PlanResponse>>> ok(List<PlanResponse> data, String message) {
        return ResponseEntity.ok(wrap(data, message, HttpStatus.OK));
    }

    private ResponseEntity<Response<PlanResponse>> ok(PlanResponse data, String message) {
        return ResponseEntity.ok(wrap(data, message, HttpStatus.OK));
    }

    private <T> Response<T> wrap(T data, String message, HttpStatus status) {
        return Response.<T>builder()
                .status(status)
                .statusCode(status.value())
                .message(message)
                .service(SERVICE)
                .data(data)
                .build();
    }
}
