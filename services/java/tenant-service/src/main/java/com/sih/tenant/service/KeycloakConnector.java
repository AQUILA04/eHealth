package com.sih.tenant.service;

import lombok.Builder;
import lombok.Data;

public interface KeycloakConnector {
    void createTenantGroup(String tenantId);

    void deleteTenantGroup(String tenantId);

    void setTenantUsersStatus(String tenantId, boolean enabled);

    /**
     * Creates an admin user in the tenant group with tenant_id attribute and ADMIN_SYSTEM role.
     * Returns credentials (temporary password when generated).
     */
    AdminProvisionResult createTenantAdmin(
            String tenantId,
            String email,
            String firstName,
            String lastName
    );

    long countTenantUsers(String tenantId);

    @Data
    @Builder
    class AdminProvisionResult {
        private String userId;
        private String username;
        private String temporaryPassword;
    }
}
