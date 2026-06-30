package com.sih.empi.controller;

import com.sih.empi.dto.DeduplicationResult;
import com.sih.empi.dto.PatientIdentityRequest;
import com.sih.empi.dto.PatientIdentityResponse;
import com.sih.empi.service.PatientIdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST du mock EMPI (Enterprise Master Patient Index).
 *
 * <p>Base path : {@code /api/v1/empi/patients}
 *
 * <p>Ce contrôleur expose les opérations CRUD sur les identités patients
 * ainsi que la recherche et la déduplication.
 */
@RestController
@RequestMapping("/api/v1/empi/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientIdentityController {

    private final PatientIdentityService service;

    /**
     * Enregistre un nouveau patient avec déduplication en temps réel.
     *
     * <p>Retourne HTTP 201 avec le patient créé et les éventuels doublons détectés.
     */
    @PostMapping
    public ResponseEntity<DeduplicationResult> registerPatient(
            @Valid @RequestBody PatientIdentityRequest request) {
        log.info("POST /api/v1/empi/patients — {} {}", request.getFirstName(), request.getLastName());
        DeduplicationResult result = service.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Retourne tous les patients actifs.
     */
    @GetMapping
    public ResponseEntity<List<PatientIdentityResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Recherche full-text (nom, prénom, MRN, identifiant national).
     */
    @GetMapping("/search")
    public ResponseEntity<List<PatientIdentityResponse>> search(
            @RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(service.search(q));
    }

    /**
     * Récupère un patient par son UUID global.
     */
    @GetMapping("/{globalUuid}")
    public ResponseEntity<PatientIdentityResponse> findByGlobalUuid(
            @PathVariable String globalUuid) {
        return service.findByGlobalUuid(globalUuid)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère un patient par son MRN.
     */
    @GetMapping("/mrn/{mrn}")
    public ResponseEntity<PatientIdentityResponse> findByMrn(@PathVariable String mrn) {
        return service.findByMrn(mrn)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère un patient par son identifiant national.
     */
    @GetMapping("/national-id/{nationalId}")
    public ResponseEntity<PatientIdentityResponse> findByNationalId(
            @PathVariable String nationalId) {
        return service.findByNationalId(nationalId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Met à jour les données démographiques d'un patient.
     */
    @PutMapping("/{globalUuid}")
    public ResponseEntity<PatientIdentityResponse> updatePatient(
            @PathVariable String globalUuid,
            @Valid @RequestBody PatientIdentityRequest request) {
        return service.updatePatient(globalUuid, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Désactive (soft-delete) un patient.
     */
    @DeleteMapping("/{globalUuid}")
    public ResponseEntity<Void> deactivatePatient(@PathVariable String globalUuid) {
        boolean deactivated = service.deactivatePatient(globalUuid);
        return deactivated
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
