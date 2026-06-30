package com.sih.empi.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests unitaires pour l'algorithme de similarité de l'EMPI.
 *
 * @author Francis AHONSU
 */
@DisplayName("SimilarityUtil — Algorithmes de déduplication")
class SimilarityUtilTest {

    // ─── Jaro-Winkler ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Chaînes identiques → score = 1.0")
    void jaroWinkler_identicalStrings_returnsOne() {
        assertThat(SimilarityUtil.jaroWinkler("Dupont", "Dupont")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Chaînes vides → score = 0.0")
    void jaroWinkler_emptyStrings_returnsZero() {
        assertThat(SimilarityUtil.jaroWinkler("", "Dupont")).isEqualTo(0.0);
        assertThat(SimilarityUtil.jaroWinkler("Dupont", "")).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Null → score = 0.0")
    void jaroWinkler_nullInput_returnsZero() {
        assertThat(SimilarityUtil.jaroWinkler(null, "Dupont")).isEqualTo(0.0);
        assertThat(SimilarityUtil.jaroWinkler("Dupont", null)).isEqualTo(0.0);
    }

    @ParameterizedTest(name = "{0} vs {1} → score ≥ {2}")
    @CsvSource({
        "Jean, Jean, 1.0",
        "Jean, Jeane, 0.9",
        "Dupont, Dupon, 0.9",
        "Marie, Marie, 1.0"
    })
    @DisplayName("Variantes orthographiques proches → score élevé")
    void jaroWinkler_closeVariants_highScore(String s1, String s2, double minScore) {
        assertThat(SimilarityUtil.jaroWinkler(s1, s2)).isGreaterThanOrEqualTo(minScore);
    }

    @Test
    @DisplayName("Chaînes totalement différentes → score faible")
    void jaroWinkler_totallyDifferent_lowScore() {
        assertThat(SimilarityUtil.jaroWinkler("Dupont", "Mba")).isLessThan(0.6);
    }

    // ─── Levenshtein ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Levenshtein — chaînes identiques → distance = 0.0")
    void levenshtein_identicalStrings_returnsZero() {
        assertThat(SimilarityUtil.levenshteinNormalized("Obiang", "Obiang")).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Levenshtein — chaînes totalement différentes → distance proche de 1.0")
    void levenshtein_differentStrings_highDistance() {
        assertThat(SimilarityUtil.levenshteinNormalized("abc", "xyz")).isGreaterThan(0.5);
    }

    // ─── Score combiné ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Score combiné — noms identiques → 1.0")
    void combinedSimilarity_identical_returnsOne() {
        assertThat(SimilarityUtil.combinedSimilarity("Dupont", "Dupont"))
            .isCloseTo(1.0, within(0.01));
    }

    @Test
    @DisplayName("Score combiné — variante orthographique → score ≥ 0.85")
    void combinedSimilarity_closeVariant_highScore() {
        // "Dupont" vs "Dupon" — une lettre manquante
        assertThat(SimilarityUtil.combinedSimilarity("Dupont", "Dupon")).isGreaterThan(0.85);
    }

    @Test
    @DisplayName("Score combiné — noms différents → score < 0.5")
    void combinedSimilarity_different_lowScore() {
        assertThat(SimilarityUtil.combinedSimilarity("Dupont", "Mba")).isLessThan(0.5);
    }
}
