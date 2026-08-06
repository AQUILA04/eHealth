package com.sih.dpi.config;

import com.sih.dpi.dto.*;
import com.sih.dpi.entity.ClinicalEncounter.EncounterType;
import com.sih.dpi.entity.LabOrder.OrderType;
import com.sih.dpi.entity.LabOrder.Priority;
import com.sih.dpi.entity.MedicationOrder.Route;
import com.sih.dpi.service.ClinicalEncounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Données de test pour le DPI service en mode mock.
 */
@Component
@Profile("mock")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ClinicalEncounterService service;

    @Override
    public void run(String... args) {
        com.sih.shared.tenant.TenantContext.setCurrentTenant("hospital-a");
        try {
            log.info("DPI Mock: Initialisation des données de test...");

            // Dossier clinique 1 — Consultation cardiologie (lié à gapEncounterId=1)
            var enc1 = service.openEncounter(ClinicalEncounterRequest.builder()
                .gapEncounterId(1L)
                .patientRef("GAP-20260101-00001")
                .empiGlobalUuid("mock-empi-uuid-001")
                .encounterType(EncounterType.OUTPATIENT)
                .chiefComplaint("Douleur thoracique et essoufflement à l'effort")
                .historyOfPresentIllness("Patient de 41 ans, HTA connue depuis 5 ans, sous traitement. " +
                    "Consultation pour bilan cardiologique annuel.")
                .pastMedicalHistory("HTA, Dyslipidémie")
                .allergies("Pénicilline (urticaire)")
                .currentMedications("Amlodipine 5mg/j, Atorvastatine 20mg/j")
                .attendingPhysicianName("Dr. Ondo Mba")
                .attendingPhysicianId("PRACT-001")
                .specialty("Cardiologie")
                .build());

            // Constantes vitales
            service.recordVitalSigns(VitalSignRequest.builder()
                .clinicalEncounterId(enc1.getId())
                .temperatureCelsius(new BigDecimal("37.2"))
                .heartRateBpm(78)
                .bloodPressureSystolic(145)
                .bloodPressureDiastolic(92)
                .oxygenSaturationPercent(new BigDecimal("98.0"))
                .weightKg(new BigDecimal("82.5"))
                .heightCm(new BigDecimal("175.0"))
                .painScore(2)
                .recordedBy("IDE Mboumba")
                .build());

            // Prescription
            service.prescribeMedication(MedicationOrderRequest.builder()
                .clinicalEncounterId(enc1.getId())
                .medicationName("Amlodipine")
                .genericName("Amlodipine bésylate")
                .atcCode("C08CA01")
                .dose("10")
                .unit("mg")
                .route(Route.ORAL)
                .frequency("1 fois par jour le matin")
                .durationDays(90)
                .indication("HTA — ajustement de dose")
                .prescribedBy("Dr. Ondo Mba")
                .prescribedById("PRACT-001")
                .build());

            // Examen biologique
            service.orderExam(LabOrderRequest.builder()
                .clinicalEncounterId(enc1.getId())
                .orderType(OrderType.BIOLOGY)
                .examName("Bilan lipidique complet")
                .examCode("LOINC-57698-3")
                .indication("Surveillance dyslipidémie")
                .priority(Priority.ROUTINE)
                .orderedBy("Dr. Ondo Mba")
                .orderedById("PRACT-001")
                .build());

            // Dossier clinique 2 — Hospitalisation médecine interne (lié à gapEncounterId=2)
            var enc2 = service.openEncounter(ClinicalEncounterRequest.builder()
                .gapEncounterId(2L)
                .patientRef("GAP-20260101-00002")
                .encounterType(EncounterType.INPATIENT)
                .chiefComplaint("Douleur abdominale aiguë en fosse iliaque droite")
                .historyOfPresentIllness("Patiente de 34 ans, douleur évoluant depuis 48h, " +
                    "fièvre à 38.5°C, nausées.")
                .allergies("Aucune allergie connue")
                .attendingPhysicianName("Dr. Nzamba")
                .attendingPhysicianId("PRACT-002")
                .specialty("Chirurgie générale")
                .build());

            // Constantes vitales urgentes
            service.recordVitalSigns(VitalSignRequest.builder()
                .clinicalEncounterId(enc2.getId())
                .temperatureCelsius(new BigDecimal("38.5"))
                .heartRateBpm(102)
                .bloodPressureSystolic(118)
                .bloodPressureDiastolic(75)
                .oxygenSaturationPercent(new BigDecimal("99.0"))
                .painScore(7)
                .recordedBy("IDE Nguema")
                .build());

            // Examen imagerie
            service.orderExam(LabOrderRequest.builder()
                .clinicalEncounterId(enc2.getId())
                .orderType(OrderType.IMAGING)
                .examName("Échographie abdominale")
                .indication("Suspicion appendicite")
                .priority(Priority.URGENT)
                .orderedBy("Dr. Nzamba")
                .orderedById("PRACT-002")
                .build());

            log.info("DPI Mock: 2 dossiers cliniques avec constantes, prescriptions et examens initialisés.");
        } finally {
            com.sih.shared.tenant.TenantContext.clear();
        }
    }
}
