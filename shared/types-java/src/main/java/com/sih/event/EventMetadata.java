package com.sih.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Métadonnées d'un événement
 * Contient des informations supplémentaires pour audit et traçabilité
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Adresse IP de la source
     */
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    /**
     * User agent du client
     */
    @JsonProperty("userAgent")
    private String userAgent;
    
    /**
     * Session ID
     */
    @JsonProperty("sessionId")
    private String sessionId;
    
    /**
     * Tenant ID (pour multi-tenancy)
     */
    @JsonProperty("tenantId")
    private String tenantId;
    
    /**
     * Environnement (DEV, STAGING, PROD)
     */
    @JsonProperty("environment")
    private String environment;
    
    /**
     * Version de l'API
     */
    @JsonProperty("apiVersion")
    private String apiVersion;
    
    /**
     * Raison du changement (pour audit)
     */
    @JsonProperty("changeReason")
    private String changeReason;
    
    /**
     * Priorité de l'événement (LOW, MEDIUM, HIGH, CRITICAL)
     */
    @JsonProperty("priority")
    @Builder.Default
    private String priority = "MEDIUM";
    
    /**
     * Propriétés personnalisées
     */
    @JsonProperty("customProperties")
    @Builder.Default
    private Map<String, Object> customProperties = new HashMap<>();
}
