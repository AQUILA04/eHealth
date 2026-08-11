package com.sih.tenant.repository;

import com.sih.tenant.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    List<SubscriptionPlan> findByIsPublicTrueAndIsActiveTrueOrderBySortOrderAsc();
    List<SubscriptionPlan> findAllByOrderBySortOrderAsc();
    Optional<SubscriptionPlan> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
