package com.sih.gap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entité Encounter — Gestion des Mouvements (ADT : Admission, Discharge, Transfer).
 *
 * <p>Représente un épisode de soins : consultation externe, hospitalisation,
 * passage aux urgences. C'est le pivot entre le dossier administratif (GAP)
 * et le dossier clinique (DPI).
 *
 * <p>Conforme au Module I — Section 2.3 de la spécification SIH.
 */
@Entity
@Table(
    name = "encounter",
    indexes = {
        @Index(name = "idx_enc_patient", columnList = "patient_id"),
        @Index(name = "idx_enc_status", columnList = "status"),
        @Index(name = "idx_enc_type", columnList = "encounterType"),
        @Index(name = "idx_enc_admission_date", columnList = "admissionDate"),
        @Index(name = "idx_enc_tenant", columnList = "tenantId")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull
    private Patient patient;

    // ─── Type et statut ───────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EncounterType encounterType = EncounterType.OUTPATIENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EncounterStatus status = EncounterStatus.PLANNED;

    // ─── Admission ────────────────────────────────────────────────────────────

    @Column(nullable = false)
    private LocalDateTime admissionDate;

    /** Mode d'admission. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AdmissionType admissionType = AdmissionType.SCHEDULED;

    /** Motif d'admission / chief complaint. */
    @Column(length = 500)
    private String admissionReason;

    // ─── Localisation (Bed Management) ───────────────────────────────────────

    /** Service / département d'accueil. */
    @Column(length = 100)
    private String ward;

    /** Numéro de chambre. */
    @Column(length = 20)
    private String room;

    /** Numéro de lit. */
    @Column(length = 20)
    private String bedNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BedStatus bedStatus;

    // ─── Praticien responsable ────────────────────────────────────────────────

    @Column(length = 200)
    private String attendingPhysicianName;

    @Column(length = 50)
    private String attendingPhysicianId;

    // ─── Sortie ───────────────────────────────────────────────────────────────

    private LocalDateTime dischargeDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DischargeDisposition dischargeDisposition;

    /** Résumé de sortie (discharge summary). */
    @Column(length = 2000)
    private String dischargeSummary;

    // ─── Couverture financière ────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Patient.FinancialCoverage financialCoverage;

    @Column(length = 100)
    private String insurancePolicyNumber;

    // ─── Métadonnées ──────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ─── Enums ────────────────────────────────────────────────────────────────

    public enum EncounterType {
        OUTPATIENT, INPATIENT, EMERGENCY, DAY_SURGERY, TELECONSULTATION
    }

    public enum EncounterStatus {
        PLANNED, IN_PROGRESS, ON_HOLD, FINISHED, CANCELLED
    }

    public enum AdmissionType {
        SCHEDULED, EMERGENCY, TRANSFER, READMISSION
    }

    public enum BedStatus {
        OCCUPIED, AVAILABLE, CLEANING, MAINTENANCE, RESERVED
    }

    public enum DischargeDisposition {
        HOME, TRANSFER_INTERNAL, TRANSFER_EXTERNAL, DECEASED, LEFT_AMA, LONG_TERM_CARE
    }

    @PrePersist
    public void prePersist() {
        if (this.tenantId == null) {
            this.tenantId = com.sih.shared.tenant.TenantContext.getCurrentTenant();
        }
    }
}
