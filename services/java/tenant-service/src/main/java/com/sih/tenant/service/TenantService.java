package com.sih.tenant.service;

import com.sih.tenant.dto.TenantRequest;
import com.sih.tenant.dto.TenantResponse;
import com.sih.tenant.entity.Tenant;
import com.sih.tenant.entity.TenantStatus;
import com.sih.tenant.entity.TenantSubscription;
import com.sih.tenant.repository.TenantRepository;
import com.sih.tenant.repository.TenantSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository repository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final KeycloakConnector keycloakConnector;

    @Transactional(readOnly = true)
    public List<TenantResponse> findAll() {
        log.info("Récupération de tous les tenants");
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TenantResponse> findById(String id) {
        log.info("Recherche du tenant avec l'ID : {}", id);
        return repository.findById(id).map(this::mapToResponse);
    }

    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        log.info("Création d'un nouveau tenant avec l'ID : {}", request.getId());
        if (repository.existsById(request.getId())) {
            throw new IllegalArgumentException("Un tenant avec cet ID existe déjà : " + request.getId());
        }

        Tenant tenant = Tenant.builder()
                .id(request.getId())
                .name(request.getName())
                .domain(request.getDomain())
                .status(request.getStatus() != null ? request.getStatus() : TenantStatus.ACTIVE)
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .build();

        Tenant savedTenant = repository.save(tenant);
        
        // Keycloak provisioning
        try {
            keycloakConnector.createTenantGroup(savedTenant.getId());
        } catch (Exception e) {
            log.error("Échec de la création du groupe Keycloak pour le tenant {}. Annulation de la transaction.", savedTenant.getId());
            throw e;
        }

        log.info("Tenant créé avec succès : {}", savedTenant.getId());
        return mapToResponse(savedTenant);
    }

    @Transactional
    public Optional<TenantResponse> updateTenant(String id, TenantRequest request) {
        log.info("Mise à jour des informations du tenant : {}", id);
        return repository.findById(id).map(tenant -> {
            tenant.setName(request.getName());
            tenant.setDomain(request.getDomain());
            tenant.setContactEmail(request.getContactEmail());
            tenant.setContactPhone(request.getContactPhone());
            if (request.getStatus() != null) {
                tenant.setStatus(request.getStatus());
            }
            Tenant updated = repository.save(tenant);
            log.info("Tenant mis à jour avec succès : {}", id);
            return mapToResponse(updated);
        });
    }

    @Transactional
    public Optional<TenantResponse> updateStatus(String id, TenantStatus status) {
        log.info("Mise à jour du statut du tenant : {} -> {}", id, status);
        return repository.findById(id).map(tenant -> {
            tenant.setStatus(status);
            Tenant updated = repository.save(tenant);
            
            // Enable or disable users in Keycloak
            boolean enableUsers = (status == TenantStatus.ACTIVE);
            try {
                keycloakConnector.setTenantUsersStatus(id, enableUsers);
            } catch (Exception e) {
                log.error("Échec de la mise à jour des statuts utilisateurs Keycloak pour le tenant {}.", id);
                throw e;
            }
            
            log.info("Statut du tenant mis à jour avec succès : {}", id);
            return mapToResponse(updated);
        });
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        Optional<TenantSubscription> sub = subscriptionRepository.findById(tenant.getId());
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .domain(tenant.getDomain())
                .status(tenant.getStatus())
                .contactEmail(tenant.getContactEmail())
                .contactPhone(tenant.getContactPhone())
                .planId(sub.map(TenantSubscription::getPlanId).orElse(null))
                .planName(sub.map(s -> s.getPlan() != null ? s.getPlan().getName() : null).orElse(null))
                .subscriptionStatus(sub.map(s -> s.getStatus().name()).orElse(null))
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
