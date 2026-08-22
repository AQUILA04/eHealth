package com.sih.lis.repository;

import com.sih.lis.entity.LaboratoryOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaboratoryOrderRepository extends JpaRepository<LaboratoryOrder, Long> {
    Optional<LaboratoryOrder> findByIdAndTenantId(Long id, String tenantId);
    List<LaboratoryOrder> findByTenantIdOrderByOrderedAtAsc(String tenantId);
    List<LaboratoryOrder> findByTenantIdAndStatusOrderByOrderedAtAsc(String tenantId, LaboratoryOrder.Status status);
    List<LaboratoryOrder> findByTenantIdAndPatientRefOrderByOrderedAtDesc(String tenantId, String patientRef);
}
