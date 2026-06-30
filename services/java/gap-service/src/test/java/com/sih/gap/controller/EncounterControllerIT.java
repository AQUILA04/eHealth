package com.sih.gap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.gap.dto.DischargeRequest;
import com.sih.gap.dto.EncounterRequest;
import com.sih.gap.dto.PatientRequest;
import com.sih.gap.entity.Encounter.AdmissionType;
import com.sih.gap.entity.Encounter.DischargeDisposition;
import com.sih.gap.entity.Encounter.EncounterType;
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
 * Tests d'intégration pour l'API GAP — Gestion ADT (Admission, Discharge, Transfer).
 *
 * @author Francis AHONSU
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@DisplayName("GAP — EncounterController ADT (Tests d'intégration)")
class EncounterControllerIT {

    private static final String PATIENT_URL = "/api/v1/gap/patients";
    private static final String ENCOUNTER_URL = "/api/v1/gap/encounters";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Crée un patient de test et retourne son ID.
     */
    private Long createTestPatient(String firstName, String lastName) throws Exception {
        PatientRequest req = PatientRequest.builder()
            .firstName(firstName)
            .lastName(lastName)
            .dateOfBirth(LocalDate.of(1980, 6, 15))
            .gender(Gender.MALE)
            .build();

        String response = mockMvc.perform(post(PATIENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @DisplayName("POST /encounters — Admission d'un patient → 201 Created")
    void admit_validRequest_returns201() throws Exception {
        Long patientId = createTestPatient("Test", "Admission");

        EncounterRequest request = EncounterRequest.builder()
            .patientId(patientId)
            .encounterType(EncounterType.INPATIENT)
            .admissionType(AdmissionType.SCHEDULED)
            .admissionReason("Chirurgie programmée")
            .ward("Chirurgie")
            .room("Chambre 5")
            .bedNumber("Lit B")
            .attendingPhysicianName("Dr. Test")
            .build();

        mockMvc.perform(post(ENCOUNTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.patientId").value(patientId))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.ward").value("Chirurgie"))
            .andExpect(jsonPath("$.bedStatus").value("OCCUPIED"));
    }

    @Test
    @DisplayName("POST /encounters — Patient inexistant → 404 Not Found")
    void admit_unknownPatient_returns404() throws Exception {
        EncounterRequest request = EncounterRequest.builder()
            .patientId(99999L)
            .encounterType(EncounterType.OUTPATIENT)
            .build();

        mockMvc.perform(post(ENCOUNTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /bed-board — Retourne les patients hospitalisés")
    void getBedBoard_returnsInProgressEncounters() throws Exception {
        mockMvc.perform(get(ENCOUNTER_URL + "/bed-board"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2)))); // 2 admissions du DataInitializer
    }

    @Test
    @DisplayName("Cycle ADT complet : Admission → Transfert → Sortie")
    void fullAdtCycle_admitTransferDischarge() throws Exception {
        Long patientId = createTestPatient("ADT", "Cycle");

        // 1. Admission
        EncounterRequest admitReq = EncounterRequest.builder()
            .patientId(patientId)
            .encounterType(EncounterType.INPATIENT)
            .admissionType(AdmissionType.EMERGENCY)
            .admissionReason("Trauma crânien")
            .ward("Urgences")
            .room("Box 1")
            .bedNumber("Lit 1")
            .attendingPhysicianName("Dr. Urgences")
            .build();

        String admitResponse = mockMvc.perform(post(ENCOUNTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(admitReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andReturn().getResponse().getContentAsString();

        Long encounterId = objectMapper.readTree(admitResponse).get("id").asLong();

        // 2. Transfert
        mockMvc.perform(patch(ENCOUNTER_URL + "/" + encounterId + "/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ward\": \"Neurologie\", \"room\": \"Chambre 10\", \"bedNumber\": \"Lit A\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ward").value("Neurologie"))
            .andExpect(jsonPath("$.room").value("Chambre 10"));

        // 3. Sortie
        DischargeRequest dischargeReq = DischargeRequest.builder()
            .dischargeDisposition(DischargeDisposition.HOME)
            .dischargeSummary("Patient stable, sortie autorisée")
            .build();

        mockMvc.perform(patch(ENCOUNTER_URL + "/" + encounterId + "/discharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dischargeReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FINISHED"))
            .andExpect(jsonPath("$.dischargeDisposition").value("HOME"))
            .andExpect(jsonPath("$.bedStatus").value("CLEANING"));
    }
}
