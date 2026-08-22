package com.sih.shared.tenant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenant(String tenantId) {
        log.debug("Setting TenantContext: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Returns the tenant bound to the current request or fails closed.
     */
    public static String requireCurrentTenant() {
        String tenantId = getCurrentTenant();
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Contexte tenant absent pour cette opération.");
        }
        return tenantId;
    }

    public static void clear() {
        log.debug("Clearing TenantContext");
        CURRENT_TENANT.remove();
    }
}
