package com.sih.empi.service;

import com.sih.empi.dto.DeduplicationResult;
import com.sih.empi.dto.DeduplicationResult.DuplicateCandidate;
import com.sih.empi.dto.PatientIdentityRequest;
import com.sih.empi.dto.PatientIdentityResponse;
import com.sih.empi.entity.PatientIdentity;
import com.sih.empi.repository.PatientIdentityRepository;
import com.sih.empi.util.SimilarityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service métier de l'EMPI (Enterprise Master Patient Index).
 *
 * <p>Responsabilités :
 * <ul>
 *   <li>Enregistrement d'une nouvelle identité patient</li>
 *   <li>Déduplication en temps réel (exacte + probabiliste)</li>
 *   <li>Recherche et récupération d'identités</li>
 *   <li>Mise à jour et désactivation d'identités</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientIdentityService {

    private final PatientIdentityRepository repository;

    @Value("${empi.matching.similarity-threshold:0.85}")
    private double similarityThreshold;

    // ─── Création ─────────────────────────────────────────────────────────────

    /**
     * Enregistre une nouvelle identité patient après déduplication.
     *
     * @param request données démographiques du patient
     * @return résultat contenant le patient créé et les doublons potentiels détectés
     */
    @Transactional
    public DeduplicationResult registerPatient(PatientIdentityRequest request) {
        log.info("EMPI: Enregistrement du patient {} {}", request.getFirstName(), request.getLastName());

        // 1. Déduplication exacte
        List<PatientIdentity> exactMatches = repository.findExactDuplicates(
            request.getFirstName(), request.getLastName(), request.getDateOfBirth()
        );

        List<DuplicateCandidate> candidates = new ArrayList<>();

        if (!exactMatches.isEmpty()) {
            log.warn("EMPI: {} doublon(s) exact(s) détecté(s) pour {} {}",
                exactMatches.size(), request.getFirstName(), request.getLastName());
            exactMatches.forEach(match -> candidates.add(DuplicateCandidate.builder()
                .patient(toResponse(match))
                .similarityScore(1.0)
                .matchType("EXACT")
                .build()));
        }

        // 2. Déduplication probabiliste (si pas de doublon exact)
        if (exactMatches.isEmpty()) {
            List<PatientIdentity> probabilisticCandidates = repository.findProbabilisticCandidates(
                request.getFirstName(), request.getLastName()
            );

            for (PatientIdentity candidate : probabilisticCandidates) {
                double firstNameScore = SimilarityUtil.combinedSimilarity(
                    request.getFirstName(), candidate.getFirstName()
                );
                double lastNameScore = SimilarityUtil.combinedSimilarity(
                    request.getLastName(), candidate.getLastName()
                );
                double overallScore = (firstNameScore + lastNameScore) / 2.0;

                if (overallScore >= similarityThreshold) {
                    log.warn("EMPI: Doublon probabiliste détecté (score={}) pour {} {}",
                        String.format("%.2f", overallScore),
                        request.getFirstName(), request.getLastName());
                    candidates.add(DuplicateCandidate.builder()
                        .patient(toResponse(candidate))
                        .similarityScore(overallScore)
                        .matchType("PROBABILISTIC")
                        .build());
                }
            }
        }

        // 3. Création de l'entité
        PatientIdentity entity = fromRequest(request);
        entity.setDuplicateSuspected(!candidates.isEmpty());
        PatientIdentity saved = repository.save(entity);

        log.info("EMPI: Patient enregistré avec globalUuid={}, mrn={}", saved.getGlobalUuid(), saved.getMrn());

        return DeduplicationResult.builder()
            .patient(toResponse(saved))
            .duplicatesFound(!candidates.isEmpty())
            .candidates(candidates)
            .build();
    }

    // ─── Lecture ──────────────────────────────────────────────────────────────

    public Optional<PatientIdentityResponse> findByGlobalUuid(String globalUuid) {
        return repository.findByGlobalUuid(globalUuid).map(this::toResponse);
    }

    public Optional<PatientIdentityResponse> findByMrn(String mrn) {
        return repository.findByMrn(mrn).map(this::toResponse);
    }

    public Optional<PatientIdentityResponse> findByNationalId(String nationalId) {
        return repository.findByNationalId(nationalId).map(this::toResponse);
    }

    public List<PatientIdentityResponse> findAll() {
        return repository.findByActiveTrue().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<PatientIdentityResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return repository.search(query.trim()).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ─── Mise à jour ──────────────────────────────────────────────────────────

    @Transactional
    public Optional<PatientIdentityResponse> updatePatient(String globalUuid, PatientIdentityRequest request) {
        return repository.findByGlobalUuid(globalUuid).map(entity -> {
            entity.setFirstName(request.getFirstName());
            entity.setLastName(request.getLastName());
            entity.setDateOfBirth(request.getDateOfBirth());
            entity.setGender(request.getGender());
            entity.setNationalId(request.getNationalId());
            entity.setPhoneNumber(request.getPhoneNumber());
            entity.setEmail(request.getEmail());
            entity.setAddress(request.getAddress());
            entity.setCity(request.getCity());
            entity.setPostalCode(request.getPostalCode());
            entity.setNationality(request.getNationality());
            return toResponse(repository.save(entity));
        });
    }

    @Transactional
    public boolean deactivatePatient(String globalUuid) {
        return repository.findByGlobalUuid(globalUuid).map(entity -> {
            entity.setActive(false);
            repository.save(entity);
            log.info("EMPI: Patient {} désactivé", globalUuid);
            return true;
        }).orElse(false);
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private PatientIdentity fromRequest(PatientIdentityRequest req) {
        return PatientIdentity.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .dateOfBirth(req.getDateOfBirth())
            .gender(req.getGender())
            .nationalId(req.getNationalId())
            .phoneNumber(req.getPhoneNumber())
            .email(req.getEmail())
            .address(req.getAddress())
            .city(req.getCity())
            .postalCode(req.getPostalCode())
            .nationality(req.getNationality())
            .build();
    }

    public PatientIdentityResponse toResponse(PatientIdentity entity) {
        return PatientIdentityResponse.builder()
            .globalUuid(entity.getGlobalUuid())
            .mrn(entity.getMrn())
            .nationalId(entity.getNationalId())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .fullName(entity.getFullName())
            .dateOfBirth(entity.getDateOfBirth())
            .gender(entity.getGender())
            .phoneNumber(entity.getPhoneNumber())
            .email(entity.getEmail())
            .address(entity.getAddress())
            .city(entity.getCity())
            .postalCode(entity.getPostalCode())
            .nationality(entity.getNationality())
            .sourceSystem(entity.getSourceSystem())
            .duplicateSuspected(entity.isDuplicateSuspected())
            .confidenceScore(entity.getConfidenceScore())
            .active(entity.isActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
