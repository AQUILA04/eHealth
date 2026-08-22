package com.sih.pharmacy.repository;

import com.sih.pharmacy.entity.Dispensation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispensationRepository extends JpaRepository<Dispensation, Long> {
    Optional<Dispensation> findByIdAndTenantId(Long id, String tenantId);
    List<Dispensation> findByTenantIdOrderByValidatedAtDesc(String tenantId);
    List<Dispensation> findByTenantIdAndPatientRefOrderByValidatedAtDesc(String tenantId, String patientRef);
}
