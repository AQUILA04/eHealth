package com.sih.gap.config;

import com.sih.gap.dto.AppointmentRequest;
import com.sih.gap.dto.EncounterRequest;
import com.sih.gap.dto.PatientRequest;
import com.sih.gap.entity.Encounter.AdmissionType;
import com.sih.gap.entity.Encounter.EncounterType;
import com.sih.gap.entity.Patient.FinancialCoverage;
import com.sih.gap.entity.Patient.Gender;
import com.sih.gap.service.AppointmentService;
import com.sih.gap.service.EncounterService;
import com.sih.gap.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Données de test pour le GAP service en mode mock.
 */
@Component
@Profile("mock")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final EncounterService encounterService;

    @Override
    public void run(String... args) {
        log.info("GAP Mock: Initialisation des données de test...");

        // Patient 1 — Consultation externe
        var p1 = patientService.registerPatient(PatientRequest.builder()
            .firstName("Jean")
            .lastName("Dupont")
            .dateOfBirth(LocalDate.of(1985, 3, 15))
            .gender(Gender.MALE)
            .empiGlobalUuid("mock-empi-uuid-001")
            .phoneNumber("+241 01 23 45 67")
            .email("jean.dupont@example.com")
            .city("Libreville")
            .nationality("Gabonaise")
            .financialCoverage(FinancialCoverage.INSURANCE)
            .insuranceCompany("CNAMGS")
            .emergencyContactName("Sophie Dupont")
            .emergencyContactPhone("+241 07 98 76 54")
            .build());

        // Patient 2 — Hospitalisé
        var p2 = patientService.registerPatient(PatientRequest.builder()
            .firstName("Marie")
            .lastName("Obiang")
            .dateOfBirth(LocalDate.of(1992, 7, 22))
            .gender(Gender.FEMALE)
            .phoneNumber("+241 06 78 90 12")
            .city("Port-Gentil")
            .nationality("Gabonaise")
            .financialCoverage(FinancialCoverage.STATE)
            .build());

        // Patient 3 — Urgences
        var p3 = patientService.registerPatient(PatientRequest.builder()
            .firstName("Paul")
            .lastName("Mba")
            .dateOfBirth(LocalDate.of(1970, 11, 5))
            .gender(Gender.MALE)
            .city("Franceville")
            .nationality("Gabonaise")
            .financialCoverage(FinancialCoverage.SELF_PAY)
            .build());

        // Rendez-vous pour p1
        appointmentService.createAppointment(AppointmentRequest.builder()
            .patientId(p1.getId())
            .scheduledTime(LocalDateTime.now().plusDays(3))
            .durationMinutes(30)
            .specialty("Cardiologie")
            .practitionerName("Dr. Ondo Mba")
            .practitionerId("PRACT-001")
            .room("Cabinet 3")
            .reason("Consultation de suivi — HTA")
            .build());

        // Admission de p2 en médecine interne
        encounterService.admit(EncounterRequest.builder()
            .patientId(p2.getId())
            .encounterType(EncounterType.INPATIENT)
            .admissionType(AdmissionType.SCHEDULED)
            .admissionReason("Chirurgie programmée — Appendicite")
            .ward("Médecine Interne")
            .room("Chambre 12")
            .bedNumber("Lit A")
            .attendingPhysicianName("Dr. Nzamba")
            .attendingPhysicianId("PRACT-002")
            .build());

        // Admission de p3 aux urgences
        encounterService.admit(EncounterRequest.builder()
            .patientId(p3.getId())
            .encounterType(EncounterType.EMERGENCY)
            .admissionType(AdmissionType.EMERGENCY)
            .admissionReason("Douleur thoracique aiguë")
            .ward("Urgences")
            .room("Box 2")
            .attendingPhysicianName("Dr. Bouanga")
            .attendingPhysicianId("PRACT-003")
            .build());

        log.info("GAP Mock: 3 patients, 1 rendez-vous, 2 admissions initialisés.");
    }
}
