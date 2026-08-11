package com.sih.tenant.config;

import com.sih.tenant.entity.BillingInterval;
import com.sih.tenant.entity.SubscriptionPlan;
import com.sih.tenant.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlanDataInitializer implements ApplicationRunner {

    public static final String PLAN_GRATUIT_ID = "00000000-0000-0000-0000-000000000001";
    public static final String PLAN_CLINIC_ID = "00000000-0000-0000-0000-000000000002";
    public static final String PLAN_GROUPE_ID = "00000000-0000-0000-0000-000000000003";

    private final SubscriptionPlanRepository planRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (planRepository.count() > 0) {
            return;
        }
        log.info("Seed des plans d'abonnement eHealth…");

        planRepository.save(SubscriptionPlan.builder()
                .id(PLAN_GRATUIT_ID)
                .name("Gratuit")
                .description("Découvrir eHealth sur un premier établissement")
                .price(BigDecimal.ZERO)
                .currency("EUR")
                .billingInterval(BillingInterval.MONTHLY)
                .isPublic(true)
                .isActive(true)
                .isFree(true)
                .autoApproveSignups(false)
                .sortOrder(1)
                .limits(gratuitLimits())
                .features(Map.of(
                        "hie_routing", false,
                        "multi_site", false,
                        "advanced_audit", false
                ))
                .build());

        planRepository.save(SubscriptionPlan.builder()
                .id(PLAN_CLINIC_ID)
                .name("Clinic")
                .description("Pour une clinique ou un service en activité")
                .price(BigDecimal.ZERO)
                .currency("EUR")
                .billingInterval(BillingInterval.MONTHLY)
                .isPublic(true)
                .isActive(true)
                .isFree(false)
                .autoApproveSignups(false)
                .sortOrder(2)
                .limits(clinicLimits())
                .features(Map.of(
                        "hie_routing", false,
                        "multi_site", false,
                        "advanced_audit", true
                ))
                .build());

        planRepository.save(SubscriptionPlan.builder()
                .id(PLAN_GROUPE_ID)
                .name("Groupe")
                .description("Multi-établissements et gouvernance partagée")
                .price(BigDecimal.ZERO)
                .currency("EUR")
                .billingInterval(BillingInterval.MONTHLY)
                .isPublic(true)
                .isActive(true)
                .isFree(false)
                .autoApproveSignups(false)
                .sortOrder(3)
                .limits(groupeLimits())
                .features(Map.of(
                        "hie_routing", true,
                        "multi_site", true,
                        "advanced_audit", true
                ))
                .build());

        log.info("Plans seedés : Gratuit, Clinic, Groupe");
    }

    private static Map<String, Object> capacity(int limit) {
        return Map.of(
                "type", "capacity",
                "windows", List.of(Map.of(
                        "period", "none",
                        "limit", limit,
                        "enforce", "hard"
                ))
        );
    }

    private static Map<String, Object> unlimitedCapacity() {
        Map<String, Object> window = new LinkedHashMap<>();
        window.put("period", "none");
        window.put("limit", null);
        window.put("enforce", "hard");
        return Map.of("type", "capacity", "windows", List.of(window));
    }

    private static Map<String, Object> usageMonthly(int limit) {
        return Map.of(
                "type", "usage",
                "windows", List.of(Map.of(
                        "period", "monthly",
                        "limit", limit,
                        "enforce", "hard"
                ))
        );
    }

    private static Map<String, Object> unlimitedUsageMonthly() {
        Map<String, Object> window = new LinkedHashMap<>();
        window.put("period", "monthly");
        window.put("limit", null);
        window.put("enforce", "hard");
        return Map.of("type", "usage", "windows", List.of(window));
    }

    private static Map<String, Object> gratuitLimits() {
        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("establishments.capacity", capacity(1));
        limits.put("users.capacity", capacity(5));
        limits.put("patients.capacity", capacity(100));
        limits.put("encounters.create", usageMonthly(30));
        limits.put("appointments.create", usageMonthly(50));
        return limits;
    }

    private static Map<String, Object> clinicLimits() {
        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("establishments.capacity", capacity(1));
        limits.put("users.capacity", capacity(50));
        limits.put("patients.capacity", unlimitedCapacity());
        limits.put("encounters.create", unlimitedUsageMonthly());
        limits.put("appointments.create", unlimitedUsageMonthly());
        return limits;
    }

    private static Map<String, Object> groupeLimits() {
        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("establishments.capacity", capacity(20));
        limits.put("users.capacity", unlimitedCapacity());
        limits.put("patients.capacity", unlimitedCapacity());
        limits.put("encounters.create", unlimitedUsageMonthly());
        limits.put("appointments.create", unlimitedUsageMonthly());
        return limits;
    }
}
