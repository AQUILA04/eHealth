package com.sih.lis.repository;

import com.sih.lis.entity.TransfusionRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransfusionRequestRepository extends JpaRepository<TransfusionRequest, Long> {
    Optional<TransfusionRequest> findByIdAndTenantId(Long id, String tenantId);
    List<TransfusionRequest> findByTenantIdOrderByRequestedAtAsc(String tenantId);
    List<TransfusionRequest> findByTenantIdAndPatientRefOrderByRequestedAtDesc(String tenantId, String patientRef);
    List<TransfusionRequest> findByTenantIdAndStatusOrderByRequestedAtAsc(String tenantId, TransfusionRequest.Status status);
}
