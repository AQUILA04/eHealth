package com.sih.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe de base pour tous les événements de domaine
 * 
 * Utilisée pour:
 * - Artemis: Transactions critiques (JMS)
 * - Kafka: Audit trail immuable
 * 
 * Chaque événement contient:
 * - ID unique pour traçabilité
 * - Timestamp pour l'ordre
 * - Actor pour audit
 * - Source pour identifier le service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PatientAdmittedEvent.class, name = "PATIENT_ADMITTED"),
    @JsonSubTypes.Type(value = PatientTransferredEvent.class, name = "PATIENT_TRANSFERRED"),
    @JsonSubTypes.Type(value = PatientDischargedEvent.class, name = "PATIENT_DISCHARGED"),
    @JsonSubTypes.Type(value = PrescriptionCreatedEvent.class, name = "PRESCRIPTION_CREATED"),
    @JsonSubTypes.Type(value = LabResultReadyEvent.class, name = "LAB_RESULT_READY"),
    @JsonSubTypes.Type(value = ImageResultReadyEvent.class, name = "IMAGE_RESULT_READY"),
})
public abstract class DomainEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * ID unique de l'événement
     */
    @JsonProperty("eventId")
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    
    /**
     * Type d'événement (PATIENT_ADMITTED, PRESCRIPTION_CREATED, etc.)
     */
    @JsonProperty("eventType")
    private String eventType;
    
    /**
     * Timestamp de l'événement
     */
    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * ID de l'utilisateur qui a déclenché l'événement
     */
    @JsonProperty("actorId")
    private String actorId;
    
    /**
     * Rôle de l'utilisateur (DOCTOR, NURSE, ADMIN, etc.)
     */
    @JsonProperty("actorRole")
    private String actorRole;
    
    /**
     * Service source de l'événement (EMPI, DPI, GAP, etc.)
     */
    @JsonProperty("source")
    private String source;
    
    /**
     * ID de l'établissement/hôpital
     */
    @JsonProperty("hospitalId")
    private String hospitalId;
    
    /**
     * Version de l'événement pour évolution
     */
    @JsonProperty("version")
    @Builder.Default
    private String version = "1.0";
    
    /**
     * Corrélation avec d'autres événements
     */
    @JsonProperty("correlationId")
    private String correlationId;
    
    /**
     * Cause de l'événement (parent event ID)
     */
    @JsonProperty("causationId")
    private String causationId;
    
    /**
     * Métadonnées additionnelles
     */
    @JsonProperty("metadata")
    private EventMetadata metadata;
    
    /**
     * Retourne le nom de la classe pour le type d'événement
     */
    public String getEventTypeName() {
        return this.getClass().getSimpleName();
    }
}
