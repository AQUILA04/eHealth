package com.sih.gap.service;

import com.sih.gap.dto.DischargeRequest;
import com.sih.gap.dto.EncounterRequest;
import com.sih.gap.dto.EncounterResponse;
import com.sih.gap.entity.Encounter;
import com.sih.gap.entity.Encounter.*;
import com.sih.gap.entity.Patient;
import com.sih.gap.entity.Patient.Gender;
import com.sih.gap.repository.EncounterRepository;
import com.sih.gap.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EncounterService (ADT).
 *
 * @author Francis AHONSU
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EncounterService — Tests unitaires")
class EncounterServiceTest {

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private EncounterService encounterService;

    private Patient testPatient;
    private Encounter testEncounter;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
            .id(1L)
            .localMrn("GAP-TEST-001")
            .firstName("Test")
            .lastName("Patient")
            .dateOfBirth(LocalDate.of(1980, 1, 1))
            .gender(Gender.MALE)
            .financialCoverage(Patient.FinancialCoverage.SELF_PAY)
            .build();

        testEncounter = Encounter.builder()
            .id(1L)
            .patient(testPatient)
            .encounterType(EncounterType.INPATIENT)
            .status(EncounterStatus.IN_PROGRESS)
            .admissionType(AdmissionType.SCHEDULED)
            .admissionDate(LocalDateTime.now())
            .ward("Médecine Interne")
            .room("Chambre 1")
            .bedNumber("Lit A")
            .bedStatus(BedStatus.OCCUPIED)
            .build();
    }

    @Test
    @DisplayName("admit — Patient existant → encounter créé avec statut IN_PROGRESS")
    void admit_existingPatient_createsEncounter() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(encounterRepository.save(any())).thenReturn(testEncounter);

        EncounterRequest request = EncounterRequest.builder()
            .patientId(1L)
            .encounterType(EncounterType.INPATIENT)
            .admissionType(AdmissionType.SCHEDULED)
            .ward("Médecine Interne")
            .build();

        EncounterResponse response = encounterService.admit(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
        assertThat(response.getWard()).isEqualTo("Médecine Interne");
        verify(encounterRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("admit — Patient inexistant → EntityNotFoundException")
    void admit_unknownPatient_throwsEntityNotFoundException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        EncounterRequest request = EncounterRequest.builder()
            .patientId(99L)
            .encounterType(EncounterType.OUTPATIENT)
            .build();

        assertThatThrownBy(() -> encounterService.admit(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    @DisplayName("discharge — Encounter existant → statut FINISHED, lit en CLEANING")
    void discharge_existingEncounter_setsFinishedAndCleaning() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(testEncounter));
        when(encounterRepository.save(any())).thenReturn(testEncounter);

        DischargeRequest dischargeReq = DischargeRequest.builder()
            .dischargeDisposition(DischargeDisposition.HOME)
            .dischargeSummary("Patient guéri")
            .build();

        Optional<EncounterResponse> result = encounterService.discharge(1L, dischargeReq);

        assertThat(result).isPresent();
        assertThat(testEncounter.getStatus()).isEqualTo(EncounterStatus.FINISHED);
        assertThat(testEncounter.getBedStatus()).isEqualTo(BedStatus.CLEANING);
        assertThat(testEncounter.getDischargeDisposition()).isEqualTo(DischargeDisposition.HOME);
    }

    @Test
    @DisplayName("transfer — Encounter existant → ward et lit mis à jour")
    void transfer_existingEncounter_updatesLocation() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(testEncounter));
        when(encounterRepository.save(any())).thenReturn(testEncounter);

        encounterService.transfer(1L, "Neurologie", "Chambre 5", "Lit B");

        assertThat(testEncounter.getWard()).isEqualTo("Neurologie");
        assertThat(testEncounter.getRoom()).isEqualTo("Chambre 5");
        assertThat(testEncounter.getBedNumber()).isEqualTo("Lit B");
    }

    @Test
    @DisplayName("getBedBoard — Retourne les encounters IN_PROGRESS")
    void getBedBoard_returnsInProgressEncounters() {
        when(encounterRepository.findByStatus(EncounterStatus.IN_PROGRESS))
            .thenReturn(List.of(testEncounter));

        List<EncounterResponse> result = encounterService.getBedBoard();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
    }
}
