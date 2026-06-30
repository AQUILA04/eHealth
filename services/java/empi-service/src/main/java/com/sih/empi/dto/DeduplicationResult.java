package com.sih.empi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Résultat du moteur de déduplication de l'EMPI.
 * Retourné lors de la création d'un patient pour informer l'opérateur.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeduplicationResult {

    /** Le patient créé ou identifié. */
    private PatientIdentityResponse patient;

    /** Indique si des doublons potentiels ont été détectés. */
    private boolean duplicatesFound;

    /** Liste des doublons potentiels avec leur score de similarité. */
    private List<DuplicateCandidate> candidates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateCandidate {
        private PatientIdentityResponse patient;
        /** Score de similarité entre 0.0 et 1.0. */
        private double similarityScore;
        /** Type de correspondance : EXACT ou PROBABILISTIC. */
        private String matchType;
    }
}
