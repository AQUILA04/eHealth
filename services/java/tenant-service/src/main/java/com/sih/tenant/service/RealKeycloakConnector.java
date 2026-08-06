package com.sih.tenant.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;

@Service
@Profile("secure")
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
            log.info("Nombre d'utilisateurs trouvés dans le groupe '{}' : {}", tenantId, members.size());

            for (UserRepresentation user : members) {
                user.setEnabled(enabled);
                keycloak.realm(realm).users().get(user.getId()).update(user);
                log.info("Utilisateur '{}' (ID: {}) mis à jour (enabled={})", user.getUsername(), user.getId(), enabled);
            }
        } catch (Exception e) {
            log.error("Erreur lors du changement de statut des utilisateurs du tenant '{}' : {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erreur d'intégration Keycloak", e);
        }
    }

    private String findGroupIdByName(String name) {
        List<GroupRepresentation> groups = keycloak.realm(realm).groups().groups(name, 0, 1);
        if (groups != null && !groups.isEmpty()) {
            return groups.get(0).getId();
        }
        return null;
    }
}
