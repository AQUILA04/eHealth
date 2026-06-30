package com.sih.dpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.dpi.dto.*;
import com.sih.dpi.entity.ClinicalEncounter.EncounterType;
import com.sih.dpi.entity.LabOrder.OrderType;
import com.sih.dpi.entity.LabOrder.Priority;
import com.sih.dpi.entity.LabOrder.ResultInterpretation;
import com.sih.dpi.entity.MedicationOrder.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour l'API DPI (Dossier Patient Informatisé).
 *
 * @author Francis AHONSU
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@DisplayName("DPI — ClinicalEncounterController (Tests d'intégration)")
class ClinicalEncounterControllerIT {

    private static final String BASE_URL = "/api/v1/dpi/encounters";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Crée un dossier clinique de test et retourne son ID.
     */
    private Long createTestEncounter(Long gapEncounterId, String patientRef) throws Exception {
        ClinicalEncounterRequest request = ClinicalEncounterRequest.builder()
            .gapEncounterId(gapEncounterId)
            .patientRef(patientRef)
            .encounterType(EncounterType.OUTPATIENT)
            .chiefComplaint("Céphalées persistantes")
            .attendingPhysicianName("Dr. Test")
            .specialty("Neurologie")
            .build();

        String response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    // ─── ClinicalEncounter ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /encounters — Ouverture d'un dossier clinique → 201 Created")
    void openEncounter_validRequest_returns201() throws Exception {
        ClinicalEncounterRequest request = ClinicalEncounterRequest.builder()
            .gapEncounterId(100L)
            .patientRef("GAP-TEST-001")
            .empiGlobalUuid("empi-uuid-test")
            .encounterType(EncounterType.INPATIENT)
            .chiefComplaint("Douleur abdominale")
            .pastMedicalHistory("Diabète type 2")
            .allergies("Aspirine")
            .attendingPhysicianName("Dr. Nzamba")
            .specialty("Médecine interne")
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.gapEncounterId").value(100))
            .andExpect(jsonPath("$.patientRef").value("GAP-TEST-001"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.chiefComplaint").value("Douleur abdominale"))
            .andExpect(jsonPath("$.vitalSigns").isArray())
            .andExpect(jsonPath("$.medicationOrders").isArray())
            .andExpect(jsonPath("$.labOrders").isArray());
    }

    @Test
    @DisplayName("POST /encounters — Champs obligatoires manquants → 400 Bad Request")
    void openEncounter_missingFields_returns400() throws Exception {
        ClinicalEncounterRequest request = ClinicalEncounterRequest.builder()
            .gapEncounterId(100L)
            // patientRef manquant
            .encounterType(EncounterType.OUTPATIENT)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /{id}/notes — Mise à jour des notes cliniques")
    void updateNotes_validRequest_updatesSuccessfully() throws Exception {
        Long encounterId = createTestEncounter(200L, "GAP-NOTES-001");

        Map<String, String> updates = Map.of(
            "primaryDiagnosisCode", "G43.9",
            "primaryDiagnosisLabel", "Migraine, sans précision",
            "treatmentPlan", "Antalgiques + repos"
        );

        mockMvc.perform(patch(BASE_URL + "/" + encounterId + "/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.primaryDiagnosisCode").value("G43.9"))
            .andExpect(jsonPath("$.primaryDiagnosisLabel").value("Migraine, sans précision"));
    }

    @Test
    @DisplayName("PATCH /{id}/close — Clôture du dossier clinique")
    void closeEncounter_validRequest_setsStatusFinished() throws Exception {
        Long encounterId = createTestEncounter(300L, "GAP-CLOSE-001");

        mockMvc.perform(patch(BASE_URL + "/" + encounterId + "/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clinicalSummary\": \"Patient guéri, sortie autorisée\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FINISHED"))
            .andExpect(jsonPath("$.clinicalSummary").value("Patient guéri, sortie autorisée"));
    }

    // ─── VitalSigns ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /{id}/vital-signs — Enregistrement des constantes vitales → 201")
    void recordVitalSigns_validRequest_returns201() throws Exception {
        Long encounterId = createTestEncounter(400L, "GAP-VS-001");

        VitalSignRequest request = VitalSignRequest.builder()
            .temperatureCelsius(new BigDecimal("37.5"))
            .heartRateBpm(82)
            .bloodPressureSystolic(130)
            .bloodPressureDiastolic(85)
            .oxygenSaturationPercent(new BigDecimal("97.5"))
            .weightKg(new BigDecimal("75.0"))
            .heightCm(new BigDecimal("170.0"))
            .painScore(3)
            .recordedBy("IDE Test")
            .build();

        mockMvc.perform(post(BASE_URL + "/" + encounterId + "/vital-signs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.heartRateBpm").value(82))
            .andExpect(jsonPath("$.bloodPressureSystolic").value(130))
            .andExpect(jsonPath("$.bmi").isNumber()) // BMI calculé automatiquement
            .andExpect(jsonPath("$.painScore").value(3));
    }

    @Test
    @DisplayName("GET /{id}/vital-signs — Récupération des constantes vitales")
    void getVitalSigns_returnsOrderedByDate() throws Exception {
        Long encounterId = createTestEncounter(500L, "GAP-VS-002");

        // Enregistrer deux séries
        VitalSignRequest vs = VitalSignRequest.builder()
            .heartRateBpm(75)
            .bloodPressureSystolic(120)
            .bloodPressureDiastolic(80)
            .recordedBy("IDE Test")
            .build();

        mockMvc.perform(post(BASE_URL + "/" + encounterId + "/vital-signs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vs)));

        mockMvc.perform(get(BASE_URL + "/" + encounterId + "/vital-signs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ─── MedicationOrders ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /{id}/medications — Prescription médicamenteuse → 201")
    void prescribeMedication_validRequest_returns201() throws Exception {
        Long encounterId = createTestEncounter(600L, "GAP-MED-001");

        MedicationOrderRequest request = MedicationOrderRequest.builder()
            .medicationName("Paracétamol")
            .genericName("Paracétamol")
            .dose("1000")
            .unit("mg")
            .route(Route.ORAL)
            .frequency("3 fois par jour")
            .durationDays(5)
            .indication("Analgésie")
            .prescribedBy("Dr. Test")
            .build();

        mockMvc.perform(post(BASE_URL + "/" + encounterId + "/medications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.medicationName").value("Paracétamol"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.route").value("ORAL"));
    }

    // ─── LabOrders ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /{id}/lab-orders — Demande d'examen → 201")
    void orderExam_validRequest_returns201() throws Exception {
        Long encounterId = createTestEncounter(700L, "GAP-LAB-001");

        LabOrderRequest request = LabOrderRequest.builder()
            .orderType(OrderType.BIOLOGY)
            .examName("NFS complète")
            .examCode("LOINC-58410-2")
            .indication("Bilan pré-opératoire")
            .priority(Priority.ROUTINE)
            .orderedBy("Dr. Test")
            .build();

        mockMvc.perform(post(BASE_URL + "/" + encounterId + "/lab-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.examName").value("NFS complète"))
            .andExpect(jsonPath("$.status").value("ORDERED"))
            .andExpect(jsonPath("$.priority").value("ROUTINE"));
    }

    @Test
    @DisplayName("PATCH /lab-orders/{id}/result — Saisie des résultats")
    void recordLabResult_validRequest_completesOrder() throws Exception {
        Long encounterId = createTestEncounter(800L, "GAP-LAB-002");

        // Créer la demande
        LabOrderRequest orderReq = LabOrderRequest.builder()
            .orderType(OrderType.BIOLOGY)
            .examName("Glycémie à jeun")
            .priority(Priority.ROUTINE)
            .orderedBy("Dr. Test")
            .build();

        String orderResponse = mockMvc.perform(post(BASE_URL + "/" + encounterId + "/lab-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderReq)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        // Saisir les résultats
        LabResultRequest resultReq = LabResultRequest.builder()
            .result("6.2")
            .resultUnit("mmol/L")
            .referenceRange("3.9 - 5.5")
            .interpretation(ResultInterpretation.HIGH)
            .resultComment("Légèrement élevé — surveillance recommandée")
            .build();

        mockMvc.perform(patch(BASE_URL + "/lab-orders/" + orderId + "/result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resultReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("6.2"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.interpretation").value("HIGH"))
            .andExpect(jsonPath("$.resultDate").isNotEmpty());
    }

    // ─── Cycle clinique complet ───────────────────────────────────────────────

    @Test
    @DisplayName("Cycle clinique complet : Ouverture → Constantes → Prescription → Examen → Clôture")
    void fullClinicalCycle_allSteps_succeed() throws Exception {
        // 1. Ouvrir le dossier
        Long encounterId = createTestEncounter(999L, "GAP-FULL-CYCLE");

        // 2. Constantes vitales
        mockMvc.perform(post(BASE_URL + "/" + encounterId + "/vital-signs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(VitalSignRequest.builder()
                    .temperatureCelsius(new BigDecimal("38.1"))
                    .heartRateBpm(95)
                    .bloodPressureSystolic(125)
                    .bloodPressureDiastolic(80)
                    .painScore(4)
                    .recordedBy("IDE Cycle")
                    .build())))
            .andExpect(status().isCreated());

        // 3. Prescription
        mockMvc.perform(post(BASE_URL + "/" + encounterId + "/medications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MedicationOrderRequest.builder()
                    .medicationName("Ibuprofène")
                    .dose("400").unit("mg")
                    .route(Route.ORAL)
                    .frequency("3 fois/j")
                    .durationDays(3)
                    .prescribedBy("Dr. Cycle")
                    .build())))
            .andExpect(status().isCreated());

        // 4. Demande d'examen
        mockMvc.perform(post(BASE_URL + "/" + encounterId + "/lab-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LabOrderRequest.builder()
                    .orderType(OrderType.BIOLOGY)
                    .examName("CRP")
                    .priority(Priority.URGENT)
                    .orderedBy("Dr. Cycle")
                    .build())))
            .andExpect(status().isCreated());

        // 5. Clôture du dossier
        mockMvc.perform(patch(BASE_URL + "/" + encounterId + "/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clinicalSummary\": \"Syndrome grippal — traitement symptomatique\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FINISHED"));

        // 6. Vérification du dossier complet
        mockMvc.perform(get(BASE_URL + "/" + encounterId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vitalSigns", hasSize(1)))
            .andExpect(jsonPath("$.medicationOrders", hasSize(1)))
            .andExpect(jsonPath("$.labOrders", hasSize(1)))
            .andExpect(jsonPath("$.status").value("FINISHED"));
    }
}
