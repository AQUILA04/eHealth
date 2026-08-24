package com.sih.shared.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

/**
 * Base persistante pour les données isolées par tenant.
 * Le tenant est résolu exclusivement depuis le contexte de requête et ne doit
 * jamais provenir d'un DTO contrôlé par le client.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class TenantScopedEntity {

    @Column(nullable = false, updatable = false)
    private String tenantId;

    @PrePersist
    protected void assignTenantOnCreate() {
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = TenantContext.requireCurrentTenant();
        }
    }
}
