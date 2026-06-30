package com.sih.empi.service;

import com.sih.empi.dto.DeduplicationResult;
import com.sih.empi.dto.PatientIdentityRequest;
import com.sih.empi.dto.PatientIdentityResponse;
import com.sih.empi.entity.PatientIdentity;
import com.sih.empi.entity.PatientIdentity.Gender;
import com.sih.empi.repository.PatientIdentityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PatientIdentityService.
 *
 * @author Francis AHONSU
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientIdentityService — Tests unitaires")
class PatientIdentityServiceTest {

    @Mock
    private PatientIdentityRepository repository;

    @InjectMocks
    private PatientIdentityService service;

    private PatientIdentity sampleEntity;
    private PatientIdentityRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleEntity = PatientIdentity.builder()
            .globalUuid("test-uuid-001")
            .mrn("MRN-001")
            .firstName("Jean")
            .lastName("Dupont")
            .dateOfBirth(LocalDate.of(1985, 3, 15))
            .gender(Gender.MALE)
            .active(true)
            .confidenceScore(1.0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        sampleRequest = PatientIdentityRequest.builder()
            .firstName("Jean")
            .lastName("Dupont")
            .dateOfBirth(LocalDate.of(1985, 3, 15))
            .gender(Gender.MALE)
            .nationalId("CNI-001")
            .build();
    }

    @Test
    @DisplayName("registerPatient — Aucun doublon → patient créé sans flag")
    void registerPatient_noDuplicates_createsPatient() {
        when(repository.findExactDuplicates(anyString(), anyString(), any())).thenReturn(Collections.emptyList());
        when(repository.findProbabilisticCandidates(anyString(), anyString())).thenReturn(Collections.emptyList());
        when(repository.save(any())).thenReturn(sampleEntity);

        DeduplicationResult result = service.registerPatient(sampleRequest);

        assertThat(result.getPatient()).isNotNull();
        assertThat(result.isDuplicatesFound()).isFalse();
        assertThat(result.getCandidates()).isEmpty();
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("registerPatient — Doublon exact → flag duplicateSuspected = true")
    void registerPatient_exactDuplicate_flagsDuplicate() {
        when(repository.findExactDuplicates(anyString(), anyString(), any()))
            .thenReturn(List.of(sampleEntity));
        when(repository.save(any())).thenReturn(sampleEntity);

        DeduplicationResult result = service.registerPatient(sampleRequest);

        assertThat(result.isDuplicatesFound()).isTrue();
        assertThat(result.getCandidates()).hasSize(1);
        assertThat(result.getCandidates().get(0).getMatchType()).isEqualTo("EXACT");
        assertThat(result.getCandidates().get(0).getSimilarityScore()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("findByGlobalUuid — UUID existant → retourne le patient")
    void findByGlobalUuid_existingUuid_returnsPatient() {
        when(repository.findByGlobalUuid("test-uuid-001")).thenReturn(Optional.of(sampleEntity));

        Optional<PatientIdentityResponse> result = service.findByGlobalUuid("test-uuid-001");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Jean");
        assertThat(result.get().getLastName()).isEqualTo("Dupont");
    }

    @Test
    @DisplayName("findByGlobalUuid — UUID inexistant → Optional vide")
    void findByGlobalUuid_unknownUuid_returnsEmpty() {
        when(repository.findByGlobalUuid(anyString())).thenReturn(Optional.empty());

        Optional<PatientIdentityResponse> result = service.findByGlobalUuid("unknown-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deactivatePatient — Patient existant → désactivé avec succès")
    void deactivatePatient_existingPatient_returnsTrue() {
        when(repository.findByGlobalUuid("test-uuid-001")).thenReturn(Optional.of(sampleEntity));
        when(repository.save(any())).thenReturn(sampleEntity);

        boolean result = service.deactivatePatient("test-uuid-001");

        assertThat(result).isTrue();
        assertThat(sampleEntity.isActive()).isFalse();
        verify(repository, times(1)).save(sampleEntity);
    }

    @Test
    @DisplayName("deactivatePatient — UUID inexistant → retourne false")
    void deactivatePatient_unknownPatient_returnsFalse() {
        when(repository.findByGlobalUuid(anyString())).thenReturn(Optional.empty());

        boolean result = service.deactivatePatient("unknown-uuid");

        assertThat(result).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("findAll — Retourne uniquement les patients actifs")
    void findAll_returnsOnlyActivePatients() {
        when(repository.findByActiveTrue()).thenReturn(List.of(sampleEntity));

        List<PatientIdentityResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }
}
