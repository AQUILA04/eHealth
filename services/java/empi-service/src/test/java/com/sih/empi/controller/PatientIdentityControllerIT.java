package com.sih.empi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.empi.dto.PatientIdentityRequest;
import com.sih.empi.entity.PatientIdentity.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour l'API EMPI.
 * Utilise H2 en mémoire et le profil "mock".
 *
 * @author Francis AHONSU
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@DisplayName("EMPI — PatientIdentityController (Tests d'intégration)")
class PatientIdentityControllerIT {

    private static final String BASE_URL = "/api/v1/empi/patients";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ─── POST /api/v1/empi/patients ───────────────────────────────────────────

    @Test
    @DisplayName("POST — Enregistrement d'un nouveau patient → 201 Created")
    void registerPatient_validRequest_returns201() throws Exception {
        PatientIdentityRequest request = PatientIdentityRequest.builder()
            .firstName("Alice")
            .lastName("Ntoutoume")
            .dateOfBirth(LocalDate.of(1990, 5, 12))
            .gender(Gender.FEMALE)
            .nationalId("CNI-TEST-001")
            .phoneNumber("+241 07 00 00 01")
            .city("Libreville")
            .nationality("Gabonaise")
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.patient.globalUuid").isNotEmpty())
            .andExpect(jsonPath("$.patient.mrn").isNotEmpty())
            .andExpect(jsonPath("$.patient.firstName").value("Alice"))
            .andExpect(jsonPath("$.patient.lastName").value("Ntoutoume"))
            .andExpect(jsonPath("$.patient.active").value(true))
            .andExpect(jsonPath("$.duplicatesFound").value(false));
    }

    @Test
    @DisplayName("POST — Champs obligatoires manquants → 400 Bad Request")
    void registerPatient_missingRequiredFields_returns400() throws Exception {
        PatientIdentityRequest request = PatientIdentityRequest.builder()
            .firstName("Alice")
            // lastName manquant
            .dateOfBirth(LocalDate.of(1990, 5, 12))
            .gender(Gender.FEMALE)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Erreur de validation"));
    }

    @Test
    @DisplayName("POST — Date de naissance dans le futur → 400 Bad Request")
    void registerPatient_futureDateOfBirth_returns400() throws Exception {
        PatientIdentityRequest request = PatientIdentityRequest.builder()
            .firstName("Bob")
            .lastName("Test")
            .dateOfBirth(LocalDate.now().plusYears(1))
            .gender(Gender.MALE)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // ─── GET /api/v1/empi/patients ────────────────────────────────────────────

    @Test
    @DisplayName("GET /patients — Retourne la liste des patients actifs")
    void findAll_returnsActivePatients() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3)))); // 3 patients du DataInitializer
    }

    // ─── GET /api/v1/empi/patients/search ─────────────────────────────────────

    @Test
    @DisplayName("GET /search — Recherche par nom → retourne les correspondances")
    void search_byLastName_returnsMatches() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search").param("q", "Dupont"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].lastName").value("Dupont"));
    }

    @Test
    @DisplayName("GET /search — Requête vide → retourne tous les patients")
    void search_emptyQuery_returnsAll() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search").param("q", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    // ─── GET /api/v1/empi/patients/{uuid} ─────────────────────────────────────

    @Test
    @DisplayName("GET /{uuid} — UUID inexistant → 404 Not Found")
    void findByGlobalUuid_unknownUuid_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/non-existent-uuid-12345"))
            .andExpect(status().isNotFound());
    }

    // ─── Cycle complet : créer → récupérer → désactiver ──────────────────────

    @Test
    @DisplayName("Cycle complet : POST → GET → DELETE")
    void fullCycle_createRetrieveDeactivate() throws Exception {
        // 1. Créer
        PatientIdentityRequest request = PatientIdentityRequest.builder()
            .firstName("Charles")
            .lastName("Moussavou")
            .dateOfBirth(LocalDate.of(1978, 9, 3))
            .gender(Gender.MALE)
            .nationalId("CNI-CYCLE-001")
            .build();

        String response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String uuid = objectMapper.readTree(response).get("patient").get("globalUuid").asText();

        // 2. Récupérer
        mockMvc.perform(get(BASE_URL + "/" + uuid))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Charles"))
            .andExpect(jsonPath("$.active").value(true));

        // 3. Désactiver
        mockMvc.perform(delete(BASE_URL + "/" + uuid))
            .andExpect(status().isNoContent());

        // 4. Vérifier que le patient n'est plus actif (soft-delete)
        mockMvc.perform(get(BASE_URL + "/" + uuid))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }

    // ─── Déduplication ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST — Doublon exact détecté → duplicatesFound = true")
    void registerPatient_exactDuplicate_flagsDuplicate() throws Exception {
        PatientIdentityRequest request = PatientIdentityRequest.builder()
            .firstName("Jean")
            .lastName("Dupont")
            .dateOfBirth(LocalDate.of(1985, 3, 15))
            .gender(Gender.MALE)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.duplicatesFound").value(true))
            .andExpect(jsonPath("$.candidates", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.candidates[0].matchType").value("EXACT"));
    }
}
