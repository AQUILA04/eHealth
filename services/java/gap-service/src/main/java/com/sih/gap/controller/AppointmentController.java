package com.sih.gap.controller;

import com.sih.gap.dto.AppointmentRequest;
import com.sih.gap.dto.AppointmentResponse;
import com.sih.gap.entity.Appointment.AppointmentStatus;
import com.sih.gap.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API REST — Gestion des rendez-vous (Scheduler — Module I, Section 2.2).
 * Base path : {@code /api/v1/gap/appointments}
 */
@RestController
@RequestMapping("/api/v1/gap/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAppointment(request));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> findByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(service.findByPeriod(start, end));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable Long id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.findByPatient(patientId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        AppointmentStatus status = AppointmentStatus.valueOf(body.get("status"));
        String reason = body.get("cancellationReason");
        return service.updateStatus(id, status, reason)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Annulé par l'opérateur") String reason) {
        return service.cancelAppointment(id, reason)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
