package com.sih.event;

/**
 * Clinical Events Package
 * 
 * This package contains all clinical-related domain events:
 * - PrescriptionCreatedEvent: Triggered when a prescription is created
 * - LabResultReadyEvent: Triggered when lab results are ready
 * - ImageResultReadyEvent: Triggered when imaging results are ready
 * 
 * All events are published to both Artemis (for transactions) and Kafka (for audit trail).
 */
public class ClinicalEvents {
    // This is a marker class for grouping clinical-related events
}
