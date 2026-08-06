package com.sih.tenant.service;

public interface KeycloakConnector {
    void createTenantGroup(String tenantId);
    void deleteTenantGroup(String tenantId);
    void setTenantUsersStatus(String tenantId, boolean enabled);
}
