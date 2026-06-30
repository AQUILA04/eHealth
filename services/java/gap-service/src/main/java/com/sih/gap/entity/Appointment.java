package com.sih.gap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entité Rendez-vous (Appointment) du GAP.
 *
 * <p>Gère la planification des consultations, examens et interventions.
 * Conforme au Module I — Section 2.2 de la spécification SIH.
 */
@Entity
@Table(
    name = "appointment",
    indexes = {
        @Index(name = "idx_appt_patient", columnList = "patient_id"),
        @Index(name = "idx_appt_scheduled_time", columnList = "scheduledTime"),
        @Index(name = "idx_appt_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull
    private Patient patient;

    // ─── Planification ────────────────────────────────────────────────────────

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    /** Durée prévue en minutes. */
    @Column(nullable = false)
    @Builder.Default
    private int durationMinutes = 30;

    /** Spécialité médicale concernée. */
    @Column(length = 100)
    private String specialty;

    /** Nom du praticien assigné. */
    @Column(length = 200)
    private String practitionerName;

    /** Identifiant du praticien (référence future vers un service RH). */
    @Column(length = 50)
    private String practitionerId;

    /** Salle / cabinet de consultation. */
    @Column(length = 50)
    private String room;

    /** Motif de la consultation. */
    @Column(length = 500)
    private String reason;

    // ─── Statut ───────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    /** Notes de l'opérateur (ex: préparation requise, instructions). */
    @Column(length = 1000)
    private String notes;

    /** Raison d'annulation si status = CANCELLED. */
    @Column(length = 500)
    private String cancellationReason;

    // ─── Métadonnées ──────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ─── Enums ────────────────────────────────────────────────────────────────

    public enum AppointmentStatus {
        /** Planifié, en attente de confirmation. */
        SCHEDULED,
        /** Confirmé par le patient. */
        CONFIRMED,
        /** Patient arrivé, en salle d'attente. */
        ARRIVED,
        /** Consultation en cours. */
        IN_PROGRESS,
        /** Consultation terminée. */
        COMPLETED,
        /** Annulé. */
        CANCELLED,
        /** Patient absent (no-show). */
        NO_SHOW
    }
}
