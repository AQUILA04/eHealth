package com.sih.tenant.service;

import com.sih.tenant.entity.*;
import com.sih.tenant.repository.TenantRepository;
import com.sih.tenant.repository.TenantSubscriptionRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantProvisioningService {

    private final TenantRepository tenantRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final KeycloakConnector keycloakConnector;

    @Transactional
    public ProvisionResult provision(TenantSignupRequest request, SubscriptionPlan plan) {
        String tenantId = request.getTenantId();
        if (tenantId != null && !tenantId.isBlank() && tenantRepository.existsById(tenantId)) {
            log.info("Provisionnement idempotent: tenant {} déjà créé", tenantId);
            return ProvisionResult.builder()
                    .tenantId(tenantId)
                    .alreadyExisted(true)
                    .build();
        }

        String subdomain = request.getSubdomain() != null && !request.getSubdomain().isBlank()
                ? slugify(request.getSubdomain())
                : slugify(request.getOrganizationName());
        tenantId = uniqueTenantId(subdomain);

        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name(request.getOrganizationName())
                .domain(tenantId + ".ehealth.saas")
                .status(TenantStatus.ACTIVE)
                .contactEmail(request.getAdminEmail())
                .contactPhone(request.getAdminPhone())
                .build();

        tenantRepository.save(tenant);

        try {
            keycloakConnector.createTenantGroup(tenantId);
            KeycloakConnector.AdminProvisionResult admin = keycloakConnector.createTenantAdmin(
                    tenantId,
                    request.getAdminEmail(),
                    request.getAdminFirstName(),
                    request.getAdminLastName()
            );

            TenantSubscription subscription = TenantSubscription.builder()
                    .tenantId(tenantId)
                    .planId(plan.getId())
                    .status(plan.isFree() ? SubscriptionStatus.ACTIVE : SubscriptionStatus.TRIAL)
                    .billingInterval(plan.getBillingInterval())
                    .currentPeriodStart(LocalDateTime.now())
                    .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                    .build();
            subscriptionRepository.save(subscription);

            return ProvisionResult.builder()
                    .tenantId(tenantId)
                    .temporaryPassword(admin.getTemporaryPassword())
                    .adminUsername(admin.getUsername())
                    .alreadyExisted(false)
                    .build();
        } catch (RuntimeException e) {
            log.error("Échec provisionnement tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        }
    }

    private String uniqueTenantId(String base) {
        String candidate = base;
        int i = 1;
        while (tenantRepository.existsById(candidate)) {
            candidate = base + "-" + i++;
            if (i > 100) {
                return base + "-" + UUID.randomUUID().toString().substring(0, 8);
            }
        }
        return candidate;
    }

    static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (normalized.isBlank()) {
            return "etablissement";
        }
        return normalized.length() > 40 ? normalized.substring(0, 40) : normalized;
    }

    @Data
    @Builder
    public static class ProvisionResult {
        private String tenantId;
        private String adminUsername;
        private String temporaryPassword;
        private boolean alreadyExisted;
    }
}
