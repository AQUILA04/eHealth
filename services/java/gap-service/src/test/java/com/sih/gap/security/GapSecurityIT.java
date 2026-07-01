package com.sih.gap.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de sécurité pour le GAP service — profil {@code secure}.
 *
 * <p>Valide la matrice RBAC en utilisant des JWT mockés via
 * {@link org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors#jwt()}.
 * Aucun Keycloak réel n'est nécessaire pour ces tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("secure")
@DisplayName("GAP Service — Tests RBAC (profil secure)")
class GapSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithRoles(String... roles) {
        SimpleGrantedAuthority[] authorities = java.util.Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toArray(SimpleGrantedAuthority[]::new);
        return jwt().authorities(authorities);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Accès non authentifié
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Accès non authentifié")
    class UnauthenticatedAccess {

        @Test
        @DisplayName("GET /actuator/health — accessible sans token")
        void actuatorHealthIsPublic() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/gap/patients — refusé sans token (401)")
        void patientsRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/v1/gap/patients"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/gap/encounters — refusé sans token (401)")
        void encountersRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/v1/gap/encounters"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Patients — RBAC
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Patients — RBAC")
    class PatientsRbac {

        @Test
        @DisplayName("MEDECIN peut lire les patients")
        void medecinCanReadPatients() throws Exception {
            mockMvc.perform(get("/api/v1/gap/patients")
                    .with(jwtWithRole("MEDECIN")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("INFIRMIER peut lire les patients")
        void infirmierCanReadPatients() throws Exception {
            mockMvc.perform(get("/api/v1/gap/patients")
                    .with(jwtWithRole("INFIRMIER")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN_GAP peut créer un patient")
        void adminGapCanCreatePatient() throws Exception {
            String body = """
                    {
                      "firstName": "Test",
                      "lastName": "Patient",
                      "dateOfBirth": "1990-01-01",
                      "gender": "MALE",
                      "nationalId": "TEST-SEC-001"
                    }
                    """;
            mockMvc.perform(post("/api/v1/gap/patients")
                    .with(jwtWithRole("ADMIN_GAP"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("COMPTABLE ne peut pas créer un patient (403)")
        void comptableCannotCreatePatient() throws Exception {
            String body = """
                    {
                      "firstName": "Test",
                      "lastName": "Patient",
                      "dateOfBirth": "1990-01-01",
                      "gender": "MALE",
                      "nationalId": "TEST-SEC-002"
                    }
                    """;
            mockMvc.perform(post("/api/v1/gap/patients")
                    .with(jwtWithRole("COMPTABLE"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PATIENT ne peut pas créer un patient (403)")
        void patientRoleCannotCreatePatient() throws Exception {
            String body = """
                    {
                      "firstName": "Test",
                      "lastName": "Patient",
                      "dateOfBirth": "1990-01-01",
                      "gender": "MALE",
                      "nationalId": "TEST-SEC-003"
                    }
                    """;
            mockMvc.perform(post("/api/v1/gap/patients")
                    .with(jwtWithRole("PATIENT"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("MEDECIN ne peut pas supprimer un patient (403)")
        void medecinCannotDeletePatient() throws Exception {
            mockMvc.perform(delete("/api/v1/gap/patients/00000000-0000-0000-0000-000000000001")
                    .with(jwtWithRole("MEDECIN")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("SUPER_ADMIN peut supprimer un patient")
        void superAdminCanDeletePatient() throws Exception {
            // L'ID est un Long dans le contrôleur — utiliser un entier
            mockMvc.perform(delete("/api/v1/gap/patients/99999")
                    .with(jwtWithRole("SUPER_ADMIN")))
                    // 404 attendu car le patient n'existe pas, mais pas 403
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Encounters (ADT) — RBAC
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Encounters (ADT) — RBAC")
    class EncountersRbac {

        @Test
        @DisplayName("MEDECIN peut lire le bed-board")
        void medecinCanReadBedBoard() throws Exception {
            mockMvc.perform(get("/api/v1/gap/encounters/bed-board")
                    .with(jwtWithRole("MEDECIN")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COMPTABLE ne peut pas lire le bed-board (403)")
        void comptableCannotReadBedBoard() throws Exception {
            mockMvc.perform(get("/api/v1/gap/encounters/bed-board")
                    .with(jwtWithRole("COMPTABLE")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PHARMACIEN ne peut pas lire le bed-board (403)")
        void pharmacienCannotReadBedBoard() throws Exception {
            mockMvc.perform(get("/api/v1/gap/encounters/bed-board")
                    .with(jwtWithRole("PHARMACIEN")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN_GAP peut accéder au bed-board")
        void adminGapCanReadBedBoard() throws Exception {
            mockMvc.perform(get("/api/v1/gap/encounters/bed-board")
                    .with(jwtWithRole("ADMIN_GAP")))
                    .andExpect(status().isOk());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Appointments — RBAC
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Appointments — RBAC")
    class AppointmentsRbac {

        @Test
        @DisplayName("PATIENT peut lire ses rendez-vous")
        void patientCanReadAppointments() throws Exception {
            // GET /appointments requiert start et end comme @RequestParam
            mockMvc.perform(get("/api/v1/gap/appointments")
                    .param("start", "2026-01-01T00:00:00")
                    .param("end", "2026-12-31T23:59:59")
                    .with(jwtWithRole("PATIENT")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COMPTABLE ne peut pas lire les rendez-vous (403)")
        void comptableCannotReadAppointments() throws Exception {
            mockMvc.perform(get("/api/v1/gap/appointments")
                    .param("start", "2026-01-01T00:00:00")
                    .param("end", "2026-12-31T23:59:59")
                    .with(jwtWithRole("COMPTABLE")))
                    .andExpect(status().isForbidden());
        }
    }
}
