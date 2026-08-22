package com.sih.lis.repository;

import com.sih.lis.entity.BloodUnit;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodUnitRepository extends JpaRepository<BloodUnit, Long> {
    Optional<BloodUnit> findByIdAndTenantId(Long id, String tenantId);
    List<BloodUnit> findByTenantIdOrderByExpiresOnAsc(String tenantId);
    List<BloodUnit> findByTenantIdAndStatusOrderByExpiresOnAsc(String tenantId, BloodUnit.Status status);
    List<BloodUnit> findByTenantIdAndStatusAndComponentAndExpiresOnGreaterThanEqualOrderByExpiresOnAsc(String tenantId, BloodUnit.Status status, BloodUnit.Component component, LocalDate expiresOn);
}
