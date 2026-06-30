package com.sih.dpi.controller;

import com.sih.dpi.dto.*;
import com.sih.dpi.entity.MedicationOrder.OrderStatus;
import com.sih.dpi.service.ClinicalEncounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API REST — Dossier Patient Informatisé (DPI — Module II).
 * Base path : {@code /api/v1/dpi/encounters}
 */
@RestController
@RequestMapping("/api/v1/dpi/encounters")
@RequiredArgsConstructor
public class ClinicalEncounterController {

    private final ClinicalEncounterService service;

    // ─── ClinicalEncounter ────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ClinicalEncounterResponse> open(
            @Valid @RequestBody ClinicalEncounterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.openEncounter(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicalEncounterResponse> findById(@PathVariable Long id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/gap/{gapEncounterId}")
    public ResponseEntity<ClinicalEncounterResponse> findByGapEncounterId(
            @PathVariable Long gapEncounterId) {
        return service.findByGapEncounterId(gapEncounterId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientRef}")
    public ResponseEntity<List<ClinicalEncounterResponse>> findByPatient(
            @PathVariable String patientRef) {
        return ResponseEntity.ok(service.findByPatient(patientRef));
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<ClinicalEncounterResponse> updateNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> updates) {
        return service.updateClinicalNotes(id, updates)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ClinicalEncounterResponse> close(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String summary = body != null ? body.get("clinicalSummary") : null;
        return service.closeEncounter(id, summary)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ─── VitalSigns ───────────────────────────────────────────────────────────

    @PostMapping("/{encounterId}/vital-signs")
    public ResponseEntity<VitalSignResponse> recordVitalSigns(
            @PathVariable Long encounterId,
            @RequestBody VitalSignRequest request) {
        request.setClinicalEncounterId(encounterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.recordVitalSigns(request));
    }

    @GetMapping("/{encounterId}/vital-signs")
    public ResponseEntity<List<VitalSignResponse>> getVitalSigns(@PathVariable Long encounterId) {
        return ResponseEntity.ok(service.getVitalSigns(encounterId));
    }

    // ─── MedicationOrders ─────────────────────────────────────────────────────

    @PostMapping("/{encounterId}/medications")
    public ResponseEntity<MedicationOrderResponse> prescribe(
            @PathVariable Long encounterId,
            @RequestBody MedicationOrderRequest request) {
        request.setClinicalEncounterId(encounterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.prescribeMedication(request));
    }

    @GetMapping("/{encounterId}/medications")
    public ResponseEntity<List<MedicationOrderResponse>> getMedications(
            @PathVariable Long encounterId) {
        return ResponseEntity.ok(service.getMedications(encounterId));
    }

    @PatchMapping("/medications/{orderId}/status")
    public ResponseEntity<MedicationOrderResponse> updateMedStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status"));
        return service.updateMedicationStatus(orderId, status)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ─── LabOrders ────────────────────────────────────────────────────────────

    @PostMapping("/{encounterId}/lab-orders")
    public ResponseEntity<LabOrderResponse> orderExam(
            @PathVariable Long encounterId,
            @RequestBody LabOrderRequest request) {
        request.setClinicalEncounterId(encounterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.orderExam(request));
    }

    @GetMapping("/{encounterId}/lab-orders")
    public ResponseEntity<List<LabOrderResponse>> getLabOrders(@PathVariable Long encounterId) {
        return ResponseEntity.ok(service.getLabOrders(encounterId));
    }

    @PatchMapping("/lab-orders/{orderId}/result")
    public ResponseEntity<LabOrderResponse> recordResult(
            @PathVariable Long orderId,
            @Valid @RequestBody LabResultRequest request) {
        return service.recordLabResult(orderId, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
