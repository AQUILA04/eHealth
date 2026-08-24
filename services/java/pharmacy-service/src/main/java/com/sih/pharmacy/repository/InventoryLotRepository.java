package com.sih.pharmacy.repository;

import com.sih.pharmacy.entity.InventoryLot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLotRepository extends JpaRepository<InventoryLot, Long> {
    Optional<InventoryLot> findByIdAndTenantId(Long id, String tenantId);
    List<InventoryLot> findByTenantIdOrderByExpiryDateAsc(String tenantId);
    List<InventoryLot> findByTenantIdAndProductIdOrderByExpiryDateAsc(String tenantId, Long productId);
    List<InventoryLot> findByTenantIdAndProductIdAndExpiryDateGreaterThanOrderByExpiryDateAsc(String tenantId, Long productId, LocalDate expiryDate);
}
