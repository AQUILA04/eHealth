package com.sih.dpi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Demande d'examen complémentaire (biologie, imagerie, etc.).
 *
 * <p>Conforme au Module II — Section 3.4 (Examens complémentaires).
 */
@Entity
@Table(
    name = "lab_order",
    indexes = {
        @Index(name = "idx_lo_encounter", columnList = "clinical_encounter_id"),
        @Index(name = "idx_lo_status", columnList = "status"),
        @Index(name = "idx_lo_type", columnList = "orderType"),
        @Index(name = "idx_lo_tenant", columnList = "tenantId")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinical_encounter_id", nullable = false)
    @NotNull
    private ClinicalEncounter clinicalEncounter;

    // ─── Examen demandé ───────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderType orderType;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String examName;

    /** Code LOINC ou code interne de l'examen. */
    @Column(length = 50)
    private String examCode;

    @Column(length = 300)
    private String indication;

    @Column(length = 500)
    private String instructions;

    /** Priorité de la demande. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Priority priority = Priority.ROUTINE;

    // ─── Résultat ─────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.ORDERED;

    /** Résultat textuel ou valeur numérique. */
    @Column(length = 3000)
    private String result;

    /** Unité du résultat. */
    @Column(length = 50)
    private String resultUnit;

    /** Valeur de référence normale. */
    @Column(length = 100)
    private String referenceRange;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ResultInterpretation interpretation;

    private LocalDateTime resultDate;

    /** Commentaires du biologiste / radiologue. */
    @Column(length = 2000)
    private String resultComment;

    // ─── Praticien demandeur ──────────────────────────────────────────────────

    @Column(length = 200)
    private String orderedBy;

    @Column(length = 50)
    private String orderedById;

    // ─── Métadonnées ──────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ─── Enums ────────────────────────────────────────────────────────────────

    public enum OrderType {
        BIOLOGY, IMAGING, PATHOLOGY, MICROBIOLOGY, CARDIOLOGY, OTHER
    }

    public enum Priority {
        ROUTINE, URGENT, STAT
    }

    public enum OrderStatus {
        ORDERED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public enum ResultInterpretation {
        NORMAL, LOW, HIGH, CRITICAL_LOW, CRITICAL_HIGH, ABNORMAL
    }

    @PrePersist
    public void prePersist() {
        if (this.tenantId == null) {
            this.tenantId = com.sih.shared.tenant.TenantContext.getCurrentTenant();
        }
    }
}
