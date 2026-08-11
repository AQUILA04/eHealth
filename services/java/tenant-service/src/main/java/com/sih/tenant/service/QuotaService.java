package com.sih.tenant.service;

import com.sih.tenant.dto.QuotaAssertRequest;
import com.sih.tenant.entity.SubscriptionStatus;
import com.sih.tenant.entity.TenantSubscription;
import com.sih.tenant.entity.TenantUsagePeriod;
import com.sih.tenant.exception.QuotaExceededException;
import com.sih.tenant.repository.TenantSubscriptionRepository;
import com.sih.tenant.repository.TenantUsagePeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuotaService {

    private final TenantSubscriptionRepository subscriptionRepository;
    private final TenantUsagePeriodRepository usageRepository;
    private final KeycloakConnector keycloakConnector;

    @Transactional
    public void assertWithinQuota(QuotaAssertRequest request) {
        TenantSubscription sub = subscriptionRepository.findById(request.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("No subscription for tenant: " + request.getTenantId()));

        if (sub.getStatus() == SubscriptionStatus.SUSPENDED || sub.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new QuotaExceededException(request.getOperation(), "subscription", 0L, 0L);
        }
        if (sub.getStatus() == SubscriptionStatus.PAST_DUE
                && sub.getGracePeriodEndsAt() != null
                && sub.getGracePeriodEndsAt().isBefore(java.time.LocalDateTime.now())) {
            throw new QuotaExceededException(request.getOperation(), "subscription", 0L, 0L);
        }

        Map<String, Object> limits = mergeLimits(sub);
        Object raw = limits.get(request.getOperation());
        if (!(raw instanceof Map<?, ?> opConfig)) {
            return;
        }

        String type = String.valueOf(opConfig.get("type"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> windows = (List<Map<String, Object>>) opConfig.get("windows");
        if (windows == null || windows.isEmpty()) {
            return;
        }

        if ("capacity".equals(type)) {
            long projected = request.getProjectedCount() != null
                    ? request.getProjectedCount()
                    : resolveCapacityCount(request.getTenantId(), request.getOperation()) + 1;
            for (Map<String, Object> window : windows) {
                if (!"hard".equals(String.valueOf(window.getOrDefault("enforce", "hard")))) {
                    continue;
                }
                Long limit = toLong(window.get("limit"));
                if (limit == null || limit < 0) {
                    continue;
                }
                if (projected > limit) {
                    throw new QuotaExceededException(
                            request.getOperation(),
                            String.valueOf(window.getOrDefault("period", "none")),
                            limit,
                            projected - 1
                    );
                }
            }
            return;
        }

        // usage
        for (Map<String, Object> window : windows) {
            if (!"hard".equals(String.valueOf(window.getOrDefault("enforce", "hard")))) {
                continue;
            }
            Long limit = toLong(window.get("limit"));
            if (limit == null || limit < 0) {
                continue;
            }
            String period = String.valueOf(window.getOrDefault("period", "monthly"));
            String periodKey = resolvePeriodKey(period);
            long current = usageRepository
                    .findByTenantIdAndOperationKeyAndPeriodTypeAndPeriodKey(
                            request.getTenantId(), request.getOperation(), period, periodKey)
                    .map(TenantUsagePeriod::getCountValue)
                    .orElse(0L);
            if (current >= limit) {
                throw new QuotaExceededException(request.getOperation(), period, limit, current);
            }
        }

        if (request.isRecordUsage()) {
            recordUsage(request.getTenantId(), request.getOperation(), windows);
        }
    }

    @Transactional
    public void recordUsage(String tenantId, String operation, List<Map<String, Object>> windows) {
        for (Map<String, Object> window : windows) {
            String period = String.valueOf(window.getOrDefault("period", "monthly"));
            String periodKey = resolvePeriodKey(period);
            TenantUsagePeriod row = usageRepository
                    .findByTenantIdAndOperationKeyAndPeriodTypeAndPeriodKey(tenantId, operation, period, periodKey)
                    .orElseGet(() -> TenantUsagePeriod.builder()
                            .tenantId(tenantId)
                            .operationKey(operation)
                            .periodType(period)
                            .periodKey(periodKey)
                            .countValue(0)
                            .build());
            row.setCountValue(row.getCountValue() + 1);
            usageRepository.save(row);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> usageSummary(String tenantId) {
        TenantSubscription sub = subscriptionRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription for tenant: " + tenantId));
        Map<String, Object> limits = mergeLimits(sub);
        Map<String, Object> summary = new HashMap<>();

        for (Map.Entry<String, Object> entry : limits.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> opConfig)) {
                continue;
            }
            String type = String.valueOf(opConfig.get("type"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> windows = (List<Map<String, Object>>) opConfig.get("windows");
            if (windows == null) {
                continue;
            }
            if ("capacity".equals(type)) {
                long current = resolveCapacityCount(tenantId, entry.getKey());
                summary.put(entry.getKey(), Map.of(
                        "type", "capacity",
                        "current", current,
                        "windows", windows
                ));
            } else {
                List<Map<String, Object>> resolved = windows.stream().map(w -> {
                    String period = String.valueOf(w.getOrDefault("period", "monthly"));
                    String periodKey = resolvePeriodKey(period);
                    long current = usageRepository
                            .findByTenantIdAndOperationKeyAndPeriodTypeAndPeriodKey(
                                    tenantId, entry.getKey(), period, periodKey)
                            .map(TenantUsagePeriod::getCountValue)
                            .orElse(0L);
                    Map<String, Object> m = new HashMap<>(w);
                    m.put("current", current);
                    m.put("periodKey", periodKey);
                    return m;
                }).toList();
                summary.put(entry.getKey(), Map.of("type", "usage", "windows", resolved));
            }
        }
        return summary;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeLimits(TenantSubscription sub) {
        Map<String, Object> merged = new HashMap<>();
        if (sub.getPlan() != null && sub.getPlan().getLimits() != null) {
            merged.putAll(sub.getPlan().getLimits());
        }
        if (sub.getCustomLimits() != null) {
            merged.putAll(sub.getCustomLimits());
        }
        return merged;
    }

    private long resolveCapacityCount(String tenantId, String operation) {
        if ("users.capacity".equals(operation)) {
            return keycloakConnector.countTenantUsers(tenantId);
        }
        // patients / establishments / others: caller must pass projectedCount
        return 0;
    }

    private static String resolvePeriodKey(String period) {
        return switch (period) {
            case "daily" -> LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            case "weekly" -> {
                LocalDate now = LocalDate.now();
                yield now.getYear() + "-W" + String.format("%02d", now.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()));
            }
            case "none" -> "capacity";
            default -> YearMonth.now().toString();
        };
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
