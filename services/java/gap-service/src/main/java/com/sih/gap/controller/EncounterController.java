package com.sih.gap.controller;

import com.sih.gap.dto.DischargeRequest;
import com.sih.gap.dto.EncounterRequest;
import com.sih.gap.dto.EncounterResponse;
import com.sih.gap.service.EncounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API REST — Gestion des mouvements ADT (Admission, Discharge, Transfer).
 * Base path : {@code /api/v1/gap/encounters}
 */
@RestController
@RequestMapping("/api/v1/gap/encounters")
@RequiredArgsConstructor
public class EncounterController {

    private final EncounterService service;

    /** Admission d'un patient. */
    @PostMapping
    public ResponseEntity<EncounterResponse> admit(@Valid @RequestBody EncounterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.admit(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EncounterResponse> findById(@PathVariable Long id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<EncounterResponse>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.findByPatient(patientId));
    }

    /** Tableau de bord des lits — tous les patients hospitalisés. */
    @GetMapping("/bed-board")
    public ResponseEntity<List<EncounterResponse>> getBedBoard() {
        return ResponseEntity.ok(service.getBedBoard());
    }

    /** Patients hospitalisés dans un service donné. */
    @GetMapping("/ward/{ward}")
    public ResponseEntity<List<EncounterResponse>> findByWard(@PathVariable String ward) {
        return ResponseEntity.ok(service.findByWard(ward));
    }

    /** Transfert interne d'un patient. */
    @PatchMapping("/{id}/transfer")
    public ResponseEntity<EncounterResponse> transfer(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return service.transfer(id, body.get("ward"), body.get("room"), body.get("bedNumber"))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /** Sortie d'un patient. */
    @PatchMapping("/{id}/discharge")
    public ResponseEntity<EncounterResponse> discharge(
            @PathVariable Long id,
            @Valid @RequestBody DischargeRequest request) {
        return service.discharge(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
