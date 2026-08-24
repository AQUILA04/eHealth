package com.sih.tenant.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Profile({"secure", "prod"})
@Slf4j
public class RealKeycloakConnector implements KeycloakConnector {

    @Value("${keycloak.server-url:http://localhost:8180}")
    private String serverUrl;

    @Value("${keycloak.realm:ehealth}")
    private String realm;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    @Value("${keycloak.admin-client-id:admin-cli}")
    private String adminClientId;

    private Keycloak keycloak;

    @PostConstruct
    public void init() {
        log.info("Initialisation du client Keycloak Admin vers : {}", serverUrl);
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .username(adminUsername)
                .password(adminPassword)
                .clientId(adminClientId)
                .build();
    }

    @PreDestroy
    public void close() {
        if (keycloak != null) {
            keycloak.close();
        }
    }

    @Override
    public void createTenantGroup(String tenantId) {
        log.info("Création du groupe Keycloak pour le tenant : {}", tenantId);
        try {
            if (findGroupIdByName(tenantId) != null) {
                log.info("Groupe Keycloak '{}' existe déjà", tenantId);
                return;
            }
            GroupRepresentation group = new GroupRepresentation();
            group.setName(tenantId);
            keycloak.realm(realm).groups().add(group);
            log.info("Groupe Keycloak '{}' créé avec succès", tenantId);
        } catch (Exception e) {
            log.error("Erreur lors de la création du groupe Keycloak '{}' : {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erreur d'intégration Keycloak", e);
        }
    }

    @Override
    public void deleteTenantGroup(String tenantId) {
        log.info("Suppression du groupe Keycloak pour le tenant : {}", tenantId);
        try {
            String groupId = findGroupIdByName(tenantId);
            if (groupId != null) {
                keycloak.realm(realm).groups().group(groupId).remove();
                log.info("Groupe Keycloak '{}' (ID: {}) supprimé avec succès", tenantId, groupId);
            } else {
                log.warn("Groupe Keycloak '{}' introuvable pour suppression", tenantId);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du groupe Keycloak '{}' : {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erreur d'intégration Keycloak", e);
        }
    }

    @Override
    public void setTenantUsersStatus(String tenantId, boolean enabled) {
        log.info("Modification du statut des utilisateurs du tenant '{}' à enabled={}", tenantId, enabled);
        try {
            String groupId = findGroupIdByName(tenantId);
            if (groupId == null) {
                log.warn("Aucun groupe Keycloak trouvé pour le tenant '{}'", tenantId);
                return;
            }

            List<UserRepresentation> members = keycloak.realm(realm).groups().group(groupId).members();
            for (UserRepresentation user : members) {
                user.setEnabled(enabled);
                keycloak.realm(realm).users().get(user.getId()).update(user);
            }
        } catch (Exception e) {
            log.error("Erreur lors du changement de statut des utilisateurs du tenant '{}' : {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erreur d'intégration Keycloak", e);
        }
    }

    @Override
    public AdminProvisionResult createTenantAdmin(String tenantId, String email, String firstName, String lastName) {
        RealmResource realmResource = keycloak.realm(realm);
        String temporaryPassword = "Welcome-" + UUID.randomUUID().toString().substring(0, 8);

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailVerified(true);
        user.setAttributes(Map.of("tenant_id", List.of(tenantId)));

        String userId;
        try (Response response = realmResource.users().create(user)) {
            if (response.getStatus() >= 300) {
                throw new RuntimeException("Keycloak create user failed: HTTP " + response.getStatus());
            }
            String location = response.getLocation() != null ? response.getLocation().getPath() : "";
            userId = location.substring(location.lastIndexOf('/') + 1);
        }

        UserResource userResource = realmResource.users().get(userId);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(true);
        credential.setValue(temporaryPassword);
        userResource.resetPassword(credential);

        String groupId = findGroupIdByName(tenantId);
        if (groupId != null) {
            userResource.joinGroup(groupId);
        }

        try {
            RoleRepresentation adminSystem = realmResource.roles().get("ADMIN_SYSTEM").toRepresentation();
            userResource.roles().realmLevel().add(List.of(adminSystem));
        } catch (Exception e) {
            log.warn("Impossible d'assigner ADMIN_SYSTEM à {}: {}", email, e.getMessage());
        }

        return AdminProvisionResult.builder()
                .userId(userId)
                .username(email)
                .temporaryPassword(temporaryPassword)
                .build();
    }

    @Override
    public long countTenantUsers(String tenantId) {
        String groupId = findGroupIdByName(tenantId);
        if (groupId == null) {
            return 0;
        }
        return keycloak.realm(realm).groups().group(groupId).members().size();
    }

    private String findGroupIdByName(String name) {
        List<GroupRepresentation> groups = keycloak.realm(realm).groups().groups(name, 0, 1);
        if (groups != null && !groups.isEmpty()) {
            return groups.get(0).getId();
        }
        return null;
    }
}
