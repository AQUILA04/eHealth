package com.sih.gap.controller;

import com.sih.gap.dto.PatientRequest;
import com.sih.gap.dto.PatientResponse;
import com.sih.gap.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API REST — Gestion des patients (GAP Module I).
 * Base path : {@code /api/v1/gap/patients}
 */
@RestController
@RequestMapping("/api/v1/gap/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService service;

    @PostMapping
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerPatient(request));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientResponse>> search(
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(service.search(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> findById(@PathVariable Long id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mrn/{mrn}")
    public ResponseEntity<PatientResponse> findByMrn(@PathVariable String mrn) {
        return service.findByMrn(mrn)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        return service.updatePatient(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/link-empi")
    public ResponseEntity<PatientResponse> linkToEmpi(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String empiUuid = body.get("empiGlobalUuid");
        if (empiUuid == null || empiUuid.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return service.linkToEmpi(id, empiUuid)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        return service.deactivatePatient(id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
