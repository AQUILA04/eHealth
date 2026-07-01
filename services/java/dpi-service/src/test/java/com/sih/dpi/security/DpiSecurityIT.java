package com.sih.dpi.security;

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
 * Tests d'intégration de sécurité pour le DPI service — profil {@code secure}.
 *
 * <p>Valide la matrice RBAC clinique en utilisant des JWT mockés.
 * Aucun Keycloak réel n'est nécessaire pour ces tests.
 *
 * <p>Mappings réels du ClinicalEncounterController :
 * <ul>
 *   <li>POST   /api/v1/dpi/encounters</li>
 *   <li>GET    /api/v1/dpi/encounters/{id}</li>
 *   <li>GET    /api/v1/dpi/encounters/patient/{patientRef}</li>
 *   <li>POST   /api/v1/dpi/encounters/{encounterId}/vital-signs</li>
 *   <li>GET    /api/v1/dpi/encounters/{encounterId}/vital-signs</li>
 *   <li>POST   /api/v1/dpi/encounters/{encounterId}/medications</li>
 *   <li>GET    /api/v1/dpi/encounters/{encounterId}/medications</li>
 *   <li>PATCH  /api/v1/dpi/encounters/medications/{orderId}/status</li>
 *   <li>POST   /api/v1/dpi/encounters/{encounterId}/lab-orders</li>
 *   <li>GET    /api/v1/dpi/encounters/{encounterId}/lab-orders</li>
 *   <li>PATCH  /api/v1/dpi/encounters/lab-orders/{orderId}/result</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("secure")
@DisplayName("DPI Service — Tests RBAC (profil secure)")
class DpiSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
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
        @DisplayName("GET /api/v1/dpi/encounters/1 — refusé sans token (401)")
        void encountersRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ClinicalEncounters — RBAC
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ClinicalEncounters — RBAC")
    class ClinicalEncountersRbac {

        @Test
        @DisplayName("MEDECIN peut lire un dossier clinique")
        void medecinCanReadEncounter() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1")
                    .with(jwtWithRole("MEDECIN")))
                    // 404 car l'encounter n'existe pas, mais pas 403
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("INFIRMIER peut lire un dossier clinique")
        void infirmierCanReadEncounter() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1")
                    .with(jwtWithRole("INFIRMIER")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PHARMACIEN peut lire un dossier clinique")
        void pharmacienCanReadEncounter() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1")
                    .with(jwtWithRole("PHARMACIEN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("COMPTABLE ne peut pas lire un dossier clinique (403)")
        void comptableCannotReadEncounters() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1")
                    .with(jwtWithRole("COMPTABLE")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN_GAP ne peut pas lire les dossiers cliniques (403)")
        void adminGapCannotReadClinicalEncounters() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1")
                    .with(jwtWithRole("ADMIN_GAP")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("INFIRMIER ne peut pas créer un dossier clinique (403)")
        void infirmierCannotCreateEncounter() throws Exception {
            String body = """
                    {
                      "gapEncounterId": 1,
                      "patientId": "PAT-001",
                      "attendingPhysicianId": "DR-001",
                      "chiefComplaint": "Test"
                    }
                    """;
            mockMvc.perform(post("/api/v1/dpi/encounters")
                    .with(jwtWithRole("INFIRMIER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VitalSigns — RBAC
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("VitalSigns — RBAC")
    class VitalSignsRbac {

        @Test
        @DisplayName("INFIRMIER peut saisir des constantes vitales")
        void infirmierCanPostVitals() throws Exception {
            String body = """
                    {
                      "clinicalEncounterId": 1,
                      "systolicBp": 120,
                      "diastolicBp": 80,
                      "heartRate": 72,
                      "temperature": 37.0,
                      "respiratoryRate": 16,
                      "oxygenSaturation": 98.0,
                      "weightKg": 70.0,
                      "heightCm": 175.0
                    }
                    """;
            mockMvc.perform(post("/api/v1/dpi/encounters/1/vital-signs")
                    .with(jwtWithRole("INFIRMIER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    // 404 car l'encounter n'existe pas, mais pas 403
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("BIOLOGISTE ne peut pas saisir des constantes vitales (403)")
        void biologisteCannotPostVitals() throws Exception {
            String body = """
                    {
                      "clinicalEncounterId": 1,
                      "systolicBp": 120,
                      "diastolicBp": 80,
                      "heartRate": 72,
                      "temperature": 37.0,
                      "respiratoryRate": 16,
                      "oxygenSaturation": 98.0,
                      "weightKg": 70.0,
                      "heightCm": 175.0
                    }
                    """;
            mockMvc.perform(post("/api/v1/dpi/encounters/1/vital-signs")
                    .with(jwtWithRole("BIOLOGISTE"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("MEDECIN peut lire les constantes vitales")
        void medecinCanReadVitals() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1/vital-signs")
                    .with(jwtWithRole("MEDECIN")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COMPTABLE ne peut pas lire les constantes vitales (403)")
        void comptableCannotReadVitals() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1/vital-signs")
                    .with(jwtWithRole("COMPTABLE")))
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MedicationOrders (CPOE) — RBAC
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("MedicationOrders (CPOE) — RBAC")
    class MedicationOrdersRbac {

        @Test
        @DisplayName("PHARMACIEN peut lire les prescriptions")
        void pharmacienCanReadMedications() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1/medications")
                    .with(jwtWithRole("PHARMACIEN")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("BIOLOGISTE ne peut pas lire les prescriptions (403)")
        void biologisteCannotReadMedications() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1/medications")
                    .with(jwtWithRole("BIOLOGISTE")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("INFIRMIER ne peut pas prescrire un médicament (403)")
        void infirmierCannotPrescribeMedication() throws Exception {
            String body = """
                    {
                      "clinicalEncounterId": 1,
                      "medicationName": "Paracétamol",
                      "dosage": "1g",
                      "frequency": "EVERY_8_HOURS",
                      "route": "ORAL",
                      "prescriberId": "DR-001"
                    }
                    """;
            mockMvc.perform(post("/api/v1/dpi/encounters/1/medications")
                    .with(jwtWithRole("INFIRMIER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LabOrders — RBAC
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("LabOrders — RBAC")
    class LabOrdersRbac {

        @Test
        @DisplayName("BIOLOGISTE peut lire les demandes d'examens")
        void biologisteCanReadLabOrders() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1/lab-orders")
                    .with(jwtWithRole("BIOLOGISTE")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PHARMACIEN ne peut pas lire les examens de labo (403)")
        void pharmacienCannotReadLabOrders() throws Exception {
            mockMvc.perform(get("/api/v1/dpi/encounters/1/lab-orders")
                    .with(jwtWithRole("PHARMACIEN")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("BIOLOGISTE peut saisir les résultats d'un examen")
        void biologisteCanPostLabResult() throws Exception {
            String body = """
                    {
                      "result": "4.5 g/dL",
                      "resultUnit": "g/dL",
                      "referenceRange": "3.5-5.0"
                    }
                    """;
            mockMvc.perform(patch("/api/v1/dpi/encounters/lab-orders/999/result")
                    .with(jwtWithRole("BIOLOGISTE"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    // 404 car ressource inexistante, mais pas 403
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("MEDECIN ne peut pas saisir les résultats d'un examen (403)")
        void medecinCannotPostLabResult() throws Exception {
            String body = """
                    {
                      "resultValue": "4.5",
                      "resultUnit": "g/dL",
                      "referenceRange": "3.5-5.0",
                      "isAbnormal": false,
                      "validatedBy": "DR-001"
                    }
                    """;
            mockMvc.perform(patch("/api/v1/dpi/encounters/lab-orders/999/result")
                    .with(jwtWithRole("MEDECIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isForbidden());
        }
    }
}
