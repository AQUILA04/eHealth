package com.sih.lis.controller;

import com.sih.lis.dto.LaboratoryDtos.*;
import com.sih.lis.entity.LaboratoryOrder;
import com.sih.lis.service.LaboratoryWorkflowService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lis/orders")
@RequiredArgsConstructor
public class LaboratoryOrderController {
    private final LaboratoryWorkflowService service;
    @PostMapping public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) { var response = service.create(request); return ResponseEntity.created(URI.create("/api/v1/lis/orders/" + response.id())).body(response); }
    @GetMapping public List<OrderResponse> list(@RequestParam(required = false) LaboratoryOrder.Status status, @RequestParam(required = false) String patientRef) { return service.list(status, patientRef); }
    @GetMapping("/{id}") public OrderResponse get(@PathVariable Long id) { return service.get(id); }
    @PatchMapping("/{id}/collect") public OrderResponse collect(@PathVariable Long id, @Valid @RequestBody CollectSpecimenRequest request) { return service.collect(id, request); }
    @PatchMapping("/{id}/receive") public OrderResponse receive(@PathVariable Long id, @Valid @RequestBody ReceiveSpecimenRequest request) { return service.receive(id, request); }
    @PostMapping("/{id}/results") public OrderResponse addResult(@PathVariable Long id, @Valid @RequestBody CreateResultRequest request) { return service.addResult(id, request); }
    @PatchMapping("/{id}/validate") public OrderResponse validate(@PathVariable Long id, @Valid @RequestBody ValidateOrderRequest request) { return service.validate(id, request); }
    @PostMapping("/{id}/critical-notification") public OrderResponse criticalNotification(@PathVariable Long id, @Valid @RequestBody CriticalNotificationRequest request) { return service.notifyCritical(id, request); }
}
