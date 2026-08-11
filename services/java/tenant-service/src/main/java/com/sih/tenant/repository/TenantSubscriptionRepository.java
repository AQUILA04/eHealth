package com.sih.tenant.repository;

import com.sih.tenant.entity.TenantSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, String> {
}
