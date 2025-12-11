package com.sih.event;

/**
 * Patient Events Package
 * 
 * This package contains all patient-related domain events:
 * - PatientAdmittedEvent: Triggered when a patient is admitted to the hospital
 * - PatientTransferredEvent: Triggered when a patient is transferred between departments
 * - PatientDischargedEvent: Triggered when a patient is discharged from the hospital
 * 
 * All events are published to both Artemis (for transactions) and Kafka (for audit trail).
 */
public class PatientEvents {
    // This is a marker class for grouping patient-related events
}
