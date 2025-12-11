package com.sih.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service de publication d'événements
 * 
 * Gère la publication sur:
 * - Artemis: Pour les transactions critiques (garantie ACID)
 * - Kafka: Pour l'audit trail immuable et le replay
 * 
 * Pattern: Event Sourcing + CQRS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {
    
    private final JmsTemplate jmsTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Publie un événement critique sur Artemis (transaction ACID)
     * 
     * Utilisé pour:
     * - Admissions patients
     * - Prescriptions médicales
     * - Transferts de patients
     * - Toute opération nécessitant une garantie ACID
     * 
     * @param event L'événement à publier
     */
    public void publishCriticalEvent(DomainEvent event) {
        try {
            // Définir le type d'événement si non défini
            if (event.getEventType() == null) {
                event.setEventType(event.getEventTypeName());
            }
            
            // Définir le timestamp si non défini
            if (event.getTimestamp() == null) {
                event.setTimestamp(LocalDateTime.now());
            }
            
            // Publier sur Artemis (JMS)
            String queueName = getQueueNameForEvent(event);
            jmsTemplate.convertAndSend(queueName, event);
            
            log.info("✅ Événement critique publié sur Artemis: {} (ID: {})", 
                event.getEventType(), event.getEventId());
            
            // Publier aussi sur Kafka pour audit trail
            publishToAuditTrail(event);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la publication de l'événement critique: {}", 
                event.getEventType(), e);
            throw new EventPublicationException("Erreur lors de la publication de l'événement", e);
        }
    }
    
    /**
     * Publie un événement non-critique sur Artemis
     * 
     * Utilisé pour:
     * - Notifications
     * - Événements secondaires
     * - Opérations asynchrones
     * 
     * @param event L'événement à publier
     */
    public void publishNonCriticalEvent(DomainEvent event) {
        try {
            if (event.getEventType() == null) {
                event.setEventType(event.getEventTypeName());
            }
            
            if (event.getTimestamp() == null) {
                event.setTimestamp(LocalDateTime.now());
            }
            
            String queueName = getQueueNameForEvent(event);
            jmsTemplate.convertAndSend(queueName, event);
            
            log.info("✅ Événement non-critique publié sur Artemis: {} (ID: {})", 
                event.getEventType(), event.getEventId());
            
            // Publier aussi sur Kafka pour audit trail
            publishToAuditTrail(event);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la publication de l'événement non-critique: {}", 
                event.getEventType(), e);
            // Ne pas lever d'exception pour les événements non-critiques
        }
    }
    
    /**
     * Publie un événement sur le topic Kafka d'audit trail
     * Immuable et rejouable
     * 
     * @param event L'événement à auditer
     */
    private void publishToAuditTrail(DomainEvent event) {
        try {
            String topicName = getTopicNameForEvent(event);
            String key = event.getHospitalId() != null ? 
                event.getHospitalId() + "-" + event.getEventId() : 
                event.getEventId();
            
            kafkaTemplate.send(topicName, key, event);
            
            log.debug("✅ Événement publié sur Kafka audit trail: {} -> {}", 
                topicName, event.getEventType());
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la publication sur Kafka: {}", 
                event.getEventType(), e);
            // Ne pas lever d'exception pour Kafka
        }
    }
    
    /**
     * Détermine le nom de la queue Artemis en fonction du type d'événement
     */
    private String getQueueNameForEvent(DomainEvent event) {
        return switch (event.getEventType()) {
            case "PATIENT_ADMITTED" -> "patient.admitted";
            case "PATIENT_TRANSFERRED" -> "patient.transferred";
            case "PATIENT_DISCHARGED" -> "patient.discharged";
            case "PRESCRIPTION_CREATED" -> "prescription.created";
            case "LAB_RESULT_READY" -> "lab.result.ready";
            case "IMAGE_RESULT_READY" -> "image.result.ready";
            default -> "events.default";
        };
    }
    
    /**
     * Détermine le topic Kafka en fonction du type d'événement
     */
    private String getTopicNameForEvent(DomainEvent event) {
        return switch (event.getEventType()) {
            case "PATIENT_ADMITTED", "PATIENT_TRANSFERRED", "PATIENT_DISCHARGED" -> 
                "ehealth.audit.patient-events";
            case "PRESCRIPTION_CREATED", "LAB_RESULT_READY", "IMAGE_RESULT_READY" -> 
                "ehealth.audit.clinical-events";
            default -> "ehealth.audit.administrative-events";
        };
    }
}

/**
 * Exception levée lors d'erreur de publication d'événement
 */
class EventPublicationException extends RuntimeException {
    public EventPublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
