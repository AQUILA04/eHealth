package com.sih.tenant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile({"mock", "unsecure", "default"})
@Slf4j
public class MockKeycloakConnector implements KeycloakConnector {

    private final Map<String, AtomicLong> userCounts = new ConcurrentHashMap<>();

    @Override
    public void createTenantGroup(String tenantId) {
        log.info("[MOCK KEYCLOAK] Création du groupe de tenant : {}", tenantId);
        userCounts.putIfAbsent(tenantId, new AtomicLong(0));
    }

    @Override
    public void deleteTenantGroup(String tenantId) {
        log.info("[MOCK KEYCLOAK] Suppression du groupe de tenant : {}", tenantId);
        userCounts.remove(tenantId);
    }

    @Override
    public void setTenantUsersStatus(String tenantId, boolean enabled) {
        log.info("[MOCK KEYCLOAK] Modification du statut des utilisateurs du tenant {} à : {}", tenantId, enabled);
    }

    @Override
    public AdminProvisionResult createTenantAdmin(String tenantId, String email, String firstName, String lastName) {
        String password = "Welcome-" + UUID.randomUUID().toString().substring(0, 8);
        userCounts.computeIfAbsent(tenantId, k -> new AtomicLong(0)).incrementAndGet();
        log.info("[MOCK KEYCLOAK] Admin créé pour tenant {} : {} / {}", tenantId, email, password);
        return AdminProvisionResult.builder()
                .userId(UUID.randomUUID().toString())
                .username(email)
                .temporaryPassword(password)
                .build();
    }

    @Override
    public long countTenantUsers(String tenantId) {
        AtomicLong count = userCounts.get(tenantId);
        return count == null ? 0 : count.get();
    }
}
