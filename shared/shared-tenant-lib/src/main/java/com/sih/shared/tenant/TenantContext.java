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

    public static void clear() {
        log.debug("Clearing TenantContext");
        CURRENT_TENANT.remove();
    }
}
