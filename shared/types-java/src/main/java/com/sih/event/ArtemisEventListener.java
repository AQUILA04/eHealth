package com.sih.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

/**
 * Service d'écoute des événements Artemis
 * 
 * Les services implémentent ces méthodes pour traiter les événements critiques
 * Chaque service peut avoir ses propres listeners
 * 
 * Exemple d'utilisation dans un service:
 * 
 * @Service
 * public class PatientEventHandler extends ArtemisEventListener {
 *     @Override
 *     @JmsListener(destination = "patient.admitted", containerFactory = "jmsListenerContainerFactory")
 *     public void handlePatientAdmitted(PatientAdmittedEvent event) {
 *         // Traiter l'admission du patient
 *     }
 * }
 */
@Slf4j
@Service
public abstract class ArtemisEventListener {
    
    /**
     * Traite l'événement d'admission patient
     * Doit être implémenté par les services intéressés
     */
    @JmsListener(destination = "patient.admitted", containerFactory = "jmsListenerContainerFactory")
    public void handlePatientAdmitted(PatientAdmittedEvent event) {
        log.info("📨 Événement reçu sur Artemis: PATIENT_ADMITTED (ID: {})", event.getEventId());
        processPatientAdmitted(event);
    }
    
    /**
     * Traite l'événement de transfert patient
     */
    @JmsListener(destination = "patient.transferred", containerFactory = "jmsListenerContainerFactory")
    public void handlePatientTransferred(Object event) {
        log.info("📨 Événement reçu sur Artemis: PATIENT_TRANSFERRED");
        processPatientTransferred(event);
    }
    
    /**
     * Traite l'événement de sortie patient
     */
    @JmsListener(destination = "patient.discharged", containerFactory = "jmsListenerContainerFactory")
    public void handlePatientDischarged(Object event) {
        log.info("📨 Événement reçu sur Artemis: PATIENT_DISCHARGED");
        processPatientDischarged(event);
    }
    
    /**
     * Traite l'événement de création de prescription
     */
    @JmsListener(destination = "prescription.created", containerFactory = "jmsListenerContainerFactory")
    public void handlePrescriptionCreated(PrescriptionCreatedEvent event) {
        log.info("📨 Événement reçu sur Artemis: PRESCRIPTION_CREATED (ID: {})", event.getEventId());
        processPrescriptionCreated(event);
    }
    
    /**
     * Traite l'événement de résultat de laboratoire
     */
    @JmsListener(destination = "lab.result.ready", containerFactory = "jmsListenerContainerFactory")
    public void handleLabResultReady(Object event) {
        log.info("📨 Événement reçu sur Artemis: LAB_RESULT_READY");
        processLabResultReady(event);
    }
    
    /**
     * Traite l'événement de résultat d'imagerie
     */
    @JmsListener(destination = "image.result.ready", containerFactory = "jmsListenerContainerFactory")
    public void handleImageResultReady(Object event) {
        log.info("📨 Événement reçu sur Artemis: IMAGE_RESULT_READY");
        processImageResultReady(event);
    }
    
    // Méthodes abstraites à implémenter par les services
    
    protected void processPatientAdmitted(PatientAdmittedEvent event) {
        // À implémenter par les services intéressés
    }
    
    protected void processPatientTransferred(Object event) {
        // À implémenter par les services intéressés
    }
    
    protected void processPatientDischarged(Object event) {
        // À implémenter par les services intéressés
    }
    
    protected void processPrescriptionCreated(PrescriptionCreatedEvent event) {
        // À implémenter par les services intéressés
    }
    
    protected void processLabResultReady(Object event) {
        // À implémenter par les services intéressés
    }
    
    protected void processImageResultReady(Object event) {
        // À implémenter par les services intéressés
    }
}
