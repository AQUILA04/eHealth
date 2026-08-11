package com.sih.gap.service;

import com.sih.gap.client.QuotaClient;
import com.sih.gap.dto.PatientRequest;
import com.sih.gap.dto.PatientResponse;
import com.sih.gap.entity.Patient;
import com.sih.gap.repository.PatientRepository;
import com.sih.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service de gestion des patients (GAP — Module I).
 *
 * <p>Orchestre l'enregistrement local et la liaison avec l'EMPI.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository repository;
    private final QuotaClient quotaClient;

    // ─── Création ─────────────────────────────────────────────────────────────

    @Transactional
    public PatientResponse registerPatient(PatientRequest request) {
        log.info("GAP: Enregistrement du patient {} {}", request.getFirstName(), request.getLastName());

        String tenantId = TenantContext.getCurrentTenant();
        long current = tenantId != null ? repository.countByTenantId(tenantId) : repository.count();
        quotaClient.assertCapacity("patients.capacity", current + 1);

        Patient patient = Patient.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .dateOfBirth(request.getDateOfBirth())
            .gender(request.getGender())
            .empiGlobalUuid(request.getEmpiGlobalUuid())
            .phoneNumber(request.getPhoneNumber())
            .email(request.getEmail())
            .address(request.getAddress())
            .city(request.getCity())
            .nationality(request.getNationality())
            .financialCoverage(request.getFinancialCoverage() != null
                ? request.getFinancialCoverage() : Patient.FinancialCoverage.SELF_PAY)
            .insurancePolicyNumber(request.getInsurancePolicyNumber())
            .insuranceCompany(request.getInsuranceCompany())
            .emergencyContactName(request.getEmergencyContactName())
            .emergencyContactPhone(request.getEmergencyContactPhone())
            .build();

        Patient saved = repository.save(patient);
        log.info("GAP: Patient enregistré avec localMrn={}, empiUuid={}",
            saved.getLocalMrn(), saved.getEmpiGlobalUuid());
        return toResponse(saved);
    }

    // ─── Lecture ──────────────────────────────────────────────────────────────

    public List<PatientResponse> findAll() {
        return repository.findByActiveTrue().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public Optional<PatientResponse> findById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public Optional<PatientResponse> findByMrn(String mrn) {
        return repository.findByLocalMrn(mrn).map(this::toResponse);
    }

    public Optional<PatientResponse> findByEmpiUuid(String empiUuid) {
        return repository.findByEmpiGlobalUuid(empiUuid).map(this::toResponse);
    }

    public List<PatientResponse> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        return repository.search(query.trim()).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ─── Mise à jour ──────────────────────────────────────────────────────────

    @Transactional
    public Optional<PatientResponse> updatePatient(Long id, PatientRequest request) {
        return repository.findById(id).map(patient -> {
            patient.setFirstName(request.getFirstName());
            patient.setLastName(request.getLastName());
            patient.setDateOfBirth(request.getDateOfBirth());
            patient.setGender(request.getGender());
            patient.setPhoneNumber(request.getPhoneNumber());
            patient.setEmail(request.getEmail());
            patient.setAddress(request.getAddress());
            patient.setCity(request.getCity());
            patient.setNationality(request.getNationality());
            if (request.getFinancialCoverage() != null) {
                patient.setFinancialCoverage(request.getFinancialCoverage());
            }
            patient.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
            patient.setInsuranceCompany(request.getInsuranceCompany());
            patient.setEmergencyContactName(request.getEmergencyContactName());
            patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
            return toResponse(repository.save(patient));
        });
    }

    @Transactional
    public Optional<PatientResponse> linkToEmpi(Long id, String empiGlobalUuid) {
        return repository.findById(id).map(patient -> {
            patient.setEmpiGlobalUuid(empiGlobalUuid);
            log.info("GAP: Patient {} lié à l'EMPI uuid={}", patient.getLocalMrn(), empiGlobalUuid);
            return toResponse(repository.save(patient));
        });
    }

    @Transactional
    public boolean deactivatePatient(Long id) {
        return repository.findById(id).map(patient -> {
            patient.setActive(false);
            repository.save(patient);
            return true;
        }).orElse(false);
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    public PatientResponse toResponse(Patient p) {
        return PatientResponse.builder()
            .id(p.getId())
            .localMrn(p.getLocalMrn())
            .empiGlobalUuid(p.getEmpiGlobalUuid())
            .firstName(p.getFirstName())
            .lastName(p.getLastName())
            .fullName(p.getFullName())
            .dateOfBirth(p.getDateOfBirth())
            .gender(p.getGender())
            .phoneNumber(p.getPhoneNumber())
            .email(p.getEmail())
            .address(p.getAddress())
            .city(p.getCity())
            .nationality(p.getNationality())
            .financialCoverage(p.getFinancialCoverage())
            .insurancePolicyNumber(p.getInsurancePolicyNumber())
            .insuranceCompany(p.getInsuranceCompany())
            .emergencyContactName(p.getEmergencyContactName())
            .emergencyContactPhone(p.getEmergencyContactPhone())
            .active(p.isActive())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }
}
