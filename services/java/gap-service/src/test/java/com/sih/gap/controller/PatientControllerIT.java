package com.sih.gap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.gap.dto.PatientRequest;
import com.sih.gap.entity.Patient.FinancialCoverage;
import com.sih.gap.entity.Patient.Gender;
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
 * Tests d'intégration pour l'API GAP — Gestion des patients.
 *
 * @author Francis AHONSU
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@DisplayName("GAP — PatientController (Tests d'intégration)")
class PatientControllerIT {

    private static final String BASE_URL = "/api/v1/gap/patients";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST — Enregistrement d'un patient → 201 Created avec MRN généré")
    void registerPatient_validRequest_returns201WithMrn() throws Exception {
        PatientRequest request = PatientRequest.builder()
            .firstName("Sophie")
            .lastName("Nkoghe")
            .dateOfBirth(LocalDate.of(1995, 8, 20))
            .gender(Gender.FEMALE)
            .city("Libreville")
            .nationality("Gabonaise")
            .financialCoverage(FinancialCoverage.INSURANCE)
            .insuranceCompany("CNAMGS")
            .emergencyContactName("Pierre Nkoghe")
            .emergencyContactPhone("+241 07 55 66 77")
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.localMrn").isNotEmpty())
            .andExpect(jsonPath("$.localMrn", startsWith("GAP-")))
            .andExpect(jsonPath("$.firstName").value("Sophie"))
            .andExpect(jsonPath("$.lastName").value("Nkoghe"))
            .andExpect(jsonPath("$.financialCoverage").value("INSURANCE"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("POST — Champs obligatoires manquants → 400 Bad Request")
    void registerPatient_missingFields_returns400() throws Exception {
        PatientRequest request = PatientRequest.builder()
            .firstName("Sophie")
            // lastName manquant
            .dateOfBirth(LocalDate.of(1995, 8, 20))
            .gender(Gender.FEMALE)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /patients — Retourne la liste des patients actifs")
    void findAll_returnsActivePatients() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("GET /search — Recherche par nom → retourne les correspondances")
    void search_byName_returnsMatches() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search").param("q", "Dupont"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].lastName").value("Dupont"));
    }

    @Test
    @DisplayName("GET /{id} — ID inexistant → 404 Not Found")
    void findById_unknownId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /{id}/link-empi — Liaison avec l'EMPI")
    void linkToEmpi_validRequest_linksSuccessfully() throws Exception {
        // Créer un patient d'abord
        PatientRequest request = PatientRequest.builder()
            .firstName("Marc")
            .lastName("Boundoukou")
            .dateOfBirth(LocalDate.of(1980, 4, 10))
            .gender(Gender.MALE)
            .build();

        String createResponse = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long patientId = objectMapper.readTree(createResponse).get("id").asLong();

        // Lier à l'EMPI
        mockMvc.perform(patch(BASE_URL + "/" + patientId + "/link-empi")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"empiGlobalUuid\": \"empi-uuid-test-999\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.empiGlobalUuid").value("empi-uuid-test-999"));
    }

    @Test
    @DisplayName("DELETE /{id} — Désactivation d'un patient (soft-delete)")
    void deactivate_existingPatient_returns204() throws Exception {
        // Créer
        PatientRequest request = PatientRequest.builder()
            .firstName("ToDelete")
            .lastName("Patient")
            .dateOfBirth(LocalDate.of(1970, 1, 1))
            .gender(Gender.MALE)
            .build();

        String response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Désactiver
        mockMvc.perform(delete(BASE_URL + "/" + id))
            .andExpect(status().isNoContent());
    }
}
