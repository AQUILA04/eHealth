package com.sih.dpi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Prescription médicamenteuse (CPOE — Computerized Physician Order Entry).
 *
 * <p>Conforme au Module II — Section 3.3 (Prescriptions).
 */
@Entity
@Table(
    name = "medication_order",
    indexes = {
        @Index(name = "idx_mo_encounter", columnList = "clinical_encounter_id"),
        @Index(name = "idx_mo_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinical_encounter_id", nullable = false)
    @NotNull
    private ClinicalEncounter clinicalEncounter;

    // ─── Médicament ───────────────────────────────────────────────────────────

    @NotBlank
    @Column(nullable = false, length = 200)
    private String medicationName;

    /** Code DCI (Dénomination Commune Internationale). */
    @Column(length = 200)
    private String genericName;

    /** Code ATC (Anatomical Therapeutic Chemical). */
    @Column(length = 20)
    private String atcCode;

    // ─── Posologie ────────────────────────────────────────────────────────────

    @Column(nullable = false, length = 50)
    private String dose;

    @Column(nullable = false, length = 50)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Route route;

    @Column(nullable = false, length = 100)
    private String frequency;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    /** Durée du traitement en jours. */
    private Integer durationDays;

    // ─── Statut et validation ─────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /** Instructions spécifiques au patient ou à la pharmacie. */
    @Column(length = 500)
    private String instructions;

    /** Raison de prescription (indication). */
    @Column(length = 300)
    private String indication;

    // ─── Praticien prescripteur ───────────────────────────────────────────────

    @Column(length = 200)
    private String prescribedBy;

    @Column(length = 50)
    private String prescribedById;

    // ─── Métadonnées ──────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ─── Enums ────────────────────────────────────────────────────────────────

    public enum Route {
        ORAL, IV, IM, SC, TOPICAL, INHALATION, SUBLINGUAL, RECTAL, NASAL, OPHTHALMIC
    }

    public enum OrderStatus {
        /** En attente de validation pharmacie. */
        PENDING,
        /** Validé et transmis à la pharmacie. */
        VALIDATED,
        /** En cours d'administration. */
        ACTIVE,
        /** Terminé. */
        COMPLETED,
        /** Annulé par le prescripteur. */
        CANCELLED,
        /** Suspendu temporairement. */
        SUSPENDED
    }
}
