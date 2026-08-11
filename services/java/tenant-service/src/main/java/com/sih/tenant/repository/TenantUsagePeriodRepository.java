package com.sih.tenant.repository;

import com.sih.tenant.entity.TenantUsagePeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantUsagePeriodRepository extends JpaRepository<TenantUsagePeriod, TenantUsagePeriod.Pk> {
    Optional<TenantUsagePeriod> findByTenantIdAndOperationKeyAndPeriodTypeAndPeriodKey(
            String tenantId, String operationKey, String periodType, String periodKey);

    List<TenantUsagePeriod> findByTenantId(String tenantId);
}
