package com.sih.tenant.service;

import com.sih.tenant.dto.PlanRequest;
import com.sih.tenant.dto.PlanResponse;
import com.sih.tenant.entity.BillingInterval;
import com.sih.tenant.entity.SubscriptionPlan;
import com.sih.tenant.repository.SubscriptionPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanResponse> listPublic() {
        return planRepository.findByIsPublicTrueAndIsActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listAll() {
        return planRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse getById(String id) {
        return planRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Plan introuvable: " + id));
    }

    @Transactional
    public PlanResponse create(PlanRequest request) {
        if (planRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Un plan avec ce nom existe déjà");
        }
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "EUR")
                .billingInterval(parseInterval(request.getBillingInterval()))
                .isPublic(request.getIsPublic() == null || request.getIsPublic())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .isFree(Boolean.TRUE.equals(request.getIsFree()))
                .autoApproveSignups(Boolean.TRUE.equals(request.getAutoApproveSignups()))
                .stripePriceId(request.getStripePriceId())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 100)
                .limits(request.getLimits() != null ? request.getLimits() : new HashMap<>())
                .features(request.getFeatures() != null ? request.getFeatures() : new HashMap<>())
                .build();
        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public PlanResponse update(String id, PlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plan introuvable: " + id));

        if (request.getName() != null) {
            plan.setName(request.getName());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getCurrency() != null) {
            plan.setCurrency(request.getCurrency());
        }
        if (request.getBillingInterval() != null) {
            plan.setBillingInterval(parseInterval(request.getBillingInterval()));
        }
        if (request.getIsPublic() != null) {
            plan.setPublic(request.getIsPublic());
        }
        if (request.getIsActive() != null) {
            plan.setActive(request.getIsActive());
        }
        if (request.getIsFree() != null) {
            plan.setFree(request.getIsFree());
        }
        if (request.getAutoApproveSignups() != null) {
            plan.setAutoApproveSignups(request.getAutoApproveSignups());
        }
        if (request.getStripePriceId() != null) {
            plan.setStripePriceId(request.getStripePriceId());
        }
        if (request.getSortOrder() != null) {
            plan.setSortOrder(request.getSortOrder());
        }
        if (request.getLimits() != null) {
            plan.setLimits(request.getLimits());
        }
        if (request.getFeatures() != null) {
            plan.setFeatures(request.getFeatures());
        }
        return toResponse(planRepository.save(plan));
    }

    private BillingInterval parseInterval(String value) {
        if (value == null) {
            return BillingInterval.MONTHLY;
        }
        return BillingInterval.valueOf(value.toUpperCase());
    }

    private PlanResponse toResponse(SubscriptionPlan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .billingInterval(plan.getBillingInterval())
                .isPublic(plan.isPublic())
                .isActive(plan.isActive())
                .isFree(plan.isFree())
                .autoApproveSignups(plan.isAutoApproveSignups())
                .stripePriceId(plan.getStripePriceId())
                .sortOrder(plan.getSortOrder())
                .limits(plan.getLimits())
                .features(plan.getFeatures())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
