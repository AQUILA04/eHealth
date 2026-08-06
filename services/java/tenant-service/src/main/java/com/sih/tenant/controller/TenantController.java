package com.sih.tenant.controller;

import com.sih.tenant.dto.Response;
import com.sih.tenant.dto.TenantRequest;
import com.sih.tenant.dto.TenantResponse;
import com.sih.tenant.entity.TenantStatus;
import com.sih.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService service;
    private static final String SERVICE_NAME = "TENANT-SERVICE";

    @PostMapping
    public ResponseEntity<Response<TenantResponse>> create(@Valid @RequestBody TenantRequest request) {
        TenantResponse data = service.createTenant(request);
        Response<TenantResponse> body = Response.<TenantResponse>builder()
                .status(HttpStatus.CREATED)
                .statusCode(HttpStatus.CREATED.value())
                .message("default.message.success")
                .service(SERVICE_NAME)
                .data(data)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<Response<List<TenantResponse>>> findAll() {
        List<TenantResponse> data = service.findAll();
        Response<List<TenantResponse>> body = Response.<List<TenantResponse>>builder()
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .message("default.message.success")
                .service(SERVICE_NAME)
                .data(data)
                .build();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<TenantResponse>> findById(@PathVariable String id) {
        return service.findById(id)
                .map(data -> {
                    Response<TenantResponse> body = Response.<TenantResponse>builder()
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .message("default.message.success")
                            .service(SERVICE_NAME)
                            .data(data)
                            .build();
                    return ResponseEntity.ok(body);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Response.<TenantResponse>builder()
                                .status(HttpStatus.NOT_FOUND)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .message("tenant.not.found")
                                .service(SERVICE_NAME)
                                .build()
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<TenantResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody TenantRequest request) {
        return service.updateTenant(id, request)
                .map(data -> {
                    Response<TenantResponse> body = Response.<TenantResponse>builder()
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .message("default.message.success")
                            .service(SERVICE_NAME)
                            .data(data)
                            .build();
                    return ResponseEntity.ok(body);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Response.<TenantResponse>builder()
                                .status(HttpStatus.NOT_FOUND)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .message("tenant.not.found")
                                .service(SERVICE_NAME)
                                .build()
                ));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Response<TenantResponse>> updateStatus(
            @PathVariable String id,
            @RequestParam TenantStatus status) {
        return service.updateStatus(id, status)
                .map(data -> {
                    Response<TenantResponse> body = Response.<TenantResponse>builder()
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .message("default.message.success")
                            .service(SERVICE_NAME)
                            .data(data)
                            .build();
                    return ResponseEntity.ok(body);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Response.<TenantResponse>builder()
                                .status(HttpStatus.NOT_FOUND)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .message("tenant.not.found")
                                .service(SERVICE_NAME)
                                .build()
                ));
    }
}
