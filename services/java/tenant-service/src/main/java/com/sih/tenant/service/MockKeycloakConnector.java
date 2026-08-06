package com.sih.tenant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"mock", "unsecure", "default"})
@Slf4j
public class MockKeycloakConnector implements KeycloakConnector {

    @Override
    public void createTenantGroup(String tenantId) {
        log.info("[MOCK KEYCLOAK] Création du groupe de tenant : {}", tenantId);
    }

    @Override
    public void deleteTenantGroup(String tenantId) {
        log.info("[MOCK KEYCLOAK] Suppression du groupe de tenant : {}", tenantId);
    }

    @Override
    public void setTenantUsersStatus(String tenantId, boolean enabled) {
        log.info("[MOCK KEYCLOAK] Modification du statut des utilisateurs du tenant {} à : {}", tenantId, enabled);
    }
}
