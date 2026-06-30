package com.sih.empi.config;

import com.sih.empi.dto.PatientIdentityRequest;
import com.sih.empi.entity.PatientIdentity.Gender;
import com.sih.empi.service.PatientIdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Initialise des données de test dans le mock EMPI au démarrage.
 * Actif uniquement avec le profil "mock" (développement).
 */
@Component
@Profile("mock")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PatientIdentityService service;

    @Override
    public void run(String... args) {
        log.info("EMPI Mock: Initialisation des données de test...");

        service.registerPatient(PatientIdentityRequest.builder()
            .firstName("Jean")
            .lastName("Dupont")
            .dateOfBirth(LocalDate.of(1985, 3, 15))
            .gender(Gender.MALE)
            .nationalId("CNI-123456789")
            .phoneNumber("+241 01 23 45 67")
            .email("jean.dupont@example.com")
            .address("12 Rue des Acacia")
            .city("Libreville")
            .postalCode("00000")
            .nationality("Gabonaise")
            .build());

        service.registerPatient(PatientIdentityRequest.builder()
            .firstName("Marie")
            .lastName("Obiang")
            .dateOfBirth(LocalDate.of(1992, 7, 22))
            .gender(Gender.FEMALE)
            .nationalId("CNI-987654321")
            .phoneNumber("+241 06 78 90 12")
            .email("marie.obiang@example.com")
            .address("45 Avenue de l'Indépendance")
            .city("Port-Gentil")
            .nationality("Gabonaise")
            .build());

        service.registerPatient(PatientIdentityRequest.builder()
            .firstName("Paul")
            .lastName("Mba")
            .dateOfBirth(LocalDate.of(1970, 11, 5))
            .gender(Gender.MALE)
            .phoneNumber("+241 07 11 22 33")
            .city("Franceville")
            .nationality("Gabonaise")
            .build());

        log.info("EMPI Mock: 3 patients de test initialisés.");
    }
}
