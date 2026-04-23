package com.sih.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Service d'écoute des événements Kafka
 * 
 * Kafka est utilisé pour:
 * - Audit trail immuable
 * - Replay d'événements
 * - Analytics et BI
 * - Conformité réglementaire
 * 
 * Les services implémentent ces méthodes pour traiter les événements d'audit
 * 
 * Exemple d'utilisation dans un service:
 * 
 * @Service
 * public class AuditEventHandler extends KafkaEventListener {
 *     @Override
 *     public void handlePatientEventAudit(DomainEvent event, String topic) {
 *         // Enregistrer l'audit du patient
 *     }
 * }
 */
@Slf4j
@Service
public abstract class KafkaEventListener {
    
    /**
     * Écoute les événements patients sur Kafka (audit trail)
     * Topic: ehealth.audit.patient-events
     */
    @KafkaListener(
        topics = "ehealth.audit.patient-events",
        groupId = "ehealth-audit-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenPatientAuditEvents(
            @Payload DomainEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("📊 Événement audit reçu de Kafka: {} (Topic: {}, Partition: {}, Offset: {})",
                event.getEventType(), topic, partition, offset);
            
            handlePatientEventAudit(event, topic);
            
            // Acknowledge manuellement
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de l'événement audit patient: {}", 
                event.getEventType(), e);
            // Ne pas acknowledge pour retry
        }
    }
    
    /**
     * Écoute les événements cliniques sur Kafka (audit trail)
     * Topic: ehealth.audit.clinical-events
     */
    @KafkaListener(
        topics = "ehealth.audit.clinical-events",
        groupId = "ehealth-audit-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenClinicalAuditEvents(
            @Payload DomainEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("📊 Événement audit clinique reçu de Kafka: {} (Offset: {})",
                event.getEventType(), offset);
            
            handleClinicalEventAudit(event, topic);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de l'événement audit clinique: {}", 
                event.getEventType(), e);
        }
    }
    
    /**
     * Écoute les événements administratifs sur Kafka (audit trail)
     * Topic: ehealth.audit.administrative-events
     */
    @KafkaListener(
        topics = "ehealth.audit.administrative-events",
        groupId = "ehealth-audit-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenAdministrativeAuditEvents(
            @Payload DomainEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("📊 Événement audit administratif reçu de Kafka: {}", event.getEventType());
            
            handleAdministrativeEventAudit(event, topic);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de l'événement audit administratif: {}", 
                event.getEventType(), e);
        }
    }
    
    /**
     * Écoute les événements analytics sur Kafka
     * Topic: ehealth.analytics.events
     */
    @KafkaListener(
        topics = "ehealth.analytics.events",
        groupId = "ehealth-analytics-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenAnalyticsEvents(
            @Payload DomainEvent event,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("📈 Événement analytics reçu de Kafka: {} (Offset: {})",
                event.getEventType(), offset);
            
            handleAnalyticsEvent(event);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de l'événement analytics: {}", 
                event.getEventType(), e);
        }
    }
    
    // Méthodes abstraites à implémenter par les services
    
    /**
     * Traite les événements patients pour l'audit
     */
    protected void handlePatientEventAudit(DomainEvent event, String topic) {
        log.debug("Audit patient: {} - {}", event.getEventType(), event.getEventId());
    }
    
    /**
     * Traite les événements cliniques pour l'audit
     */
    protected void handleClinicalEventAudit(DomainEvent event, String topic) {
        log.debug("Audit clinique: {} - {}", event.getEventType(), event.getEventId());
    }
    
    /**
     * Traite les événements administratifs pour l'audit
     */
    protected void handleAdministrativeEventAudit(DomainEvent event, String topic) {
        log.debug("Audit administratif: {} - {}", event.getEventType(), event.getEventId());
    }
    
    /**
     * Traite les événements pour l'analytics
     */
    protected void handleAnalyticsEvent(DomainEvent event) {
        log.debug("Analytics: {} - {}", event.getEventType(), event.getEventId());
    }
}
