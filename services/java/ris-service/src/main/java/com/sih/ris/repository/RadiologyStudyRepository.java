package com.sih.ris.repository;

import com.sih.ris.entity.RadiologyStudy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RadiologyStudyRepository extends JpaRepository<RadiologyStudy, Long> {
    Optional<RadiologyStudy> findByIdAndTenantId(Long id, String tenantId);
    List<RadiologyStudy> findByTenantIdOrderByRequestedAtAsc(String tenantId);
    List<RadiologyStudy> findByTenantIdAndStatusOrderByScheduledAtAsc(String tenantId, RadiologyStudy.Status status);
    List<RadiologyStudy> findByTenantIdAndPatientRefOrderByRequestedAtDesc(String tenantId, String patientRef);
}
