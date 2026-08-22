package com.sih.pharmacy.repository;

import com.sih.pharmacy.entity.MedicationProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationProductRepository extends JpaRepository<MedicationProduct, Long> {
    Optional<MedicationProduct> findByIdAndTenantId(Long id, String tenantId);
    Optional<MedicationProduct> findByTenantIdAndSku(String tenantId, String sku);
    List<MedicationProduct> findByTenantIdAndActiveTrueOrderByNameAsc(String tenantId);
}
