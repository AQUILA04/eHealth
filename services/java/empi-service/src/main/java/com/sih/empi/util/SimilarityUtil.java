package com.sih.empi.util;

/**
 * Utilitaire de calcul de similarité pour la déduplication probabiliste.
 *
 * <p>Implémente deux algorithmes :
 * <ul>
 *   <li><b>Distance de Levenshtein</b> normalisée (0.0 = identique, 1.0 = totalement différent)</li>
 *   <li><b>Similarité de Jaro-Winkler</b> (0.0 = aucune similarité, 1.0 = identique)</li>
 * </ul>
 *
 * <p>En production, ces algorithmes peuvent être remplacés par une bibliothèque
 * dédiée (Apache Commons Text, SimMetrics) ou un moteur d'indexation (Elasticsearch).
 */
public final class SimilarityUtil {

    private SimilarityUtil() {
        // Classe utilitaire, pas d'instanciation
    }

    /**
     * Calcule la similarité de Jaro-Winkler entre deux chaînes.
     *
     * @param s1 première chaîne (non nulle)
     * @param s2 deuxième chaîne (non nulle)
     * @return score entre 0.0 (aucune similarité) et 1.0 (identique)
     */
    public static double jaroWinkler(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        s1 = s1.toLowerCase().trim();
        s2 = s2.toLowerCase().trim();
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        double jaro = jaro(s1, s2);
        // Préfixe commun (max 4 caractères)
        int prefix = 0;
        int maxPrefix = Math.min(4, Math.min(s1.length(), s2.length()));
        for (int i = 0; i < maxPrefix; i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }
        return jaro + (prefix * 0.1 * (1.0 - jaro));
    }

    private static double jaro(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int matchDistance = Math.max(len1, len2) / 2 - 1;
        if (matchDistance < 0) matchDistance = 0;

        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];
        int matches = 0;
        int transpositions = 0;

        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);
            for (int j = start; j < end; j++) {
                if (s2Matches[j] || s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }

        if (matches == 0) return 0.0;

        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }

        return (matches / (double) len1
                + matches / (double) len2
                + (matches - transpositions / 2.0) / matches) / 3.0;
    }

    /**
     * Calcule la distance de Levenshtein normalisée entre deux chaînes.
     *
     * @return score entre 0.0 (identique) et 1.0 (totalement différent)
     */
    public static double levenshteinNormalized(String s1, String s2) {
        if (s1 == null || s2 == null) return 1.0;
        s1 = s1.toLowerCase().trim();
        s2 = s2.toLowerCase().trim();
        if (s1.equals(s2)) return 0.0;
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 0.0;
        return (double) levenshtein(s1, s2) / maxLen;
    }

    private static int levenshtein(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];
        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[len1][len2];
    }

    /**
     * Score de similarité combiné (moyenne pondérée Jaro-Winkler + Levenshtein inversé).
     *
     * @return score entre 0.0 (aucune similarité) et 1.0 (identique)
     */
    public static double combinedSimilarity(String s1, String s2) {
        double jw = jaroWinkler(s1, s2);
        double lev = 1.0 - levenshteinNormalized(s1, s2);
        return (jw * 0.6) + (lev * 0.4);
    }
}
