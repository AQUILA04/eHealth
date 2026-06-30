package com.sih.dpi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Constantes vitales (Vital Signs) enregistrées lors d'un épisode clinique.
 *
 * <p>Conforme au Module II — Section 3.2 (Observations cliniques).
 */
@Entity
@Table(
    name = "vital_sign",
    indexes = {
        @Index(name = "idx_vs_encounter", columnList = "clinical_encounter_id"),
        @Index(name = "idx_vs_recorded_at", columnList = "recordedAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinical_encounter_id", nullable = false)
    @NotNull
    private ClinicalEncounter clinicalEncounter;

    // ─── Constantes vitales ───────────────────────────────────────────────────

    /** Température corporelle en °C. */
    @Column(precision = 4, scale = 1)
    private BigDecimal temperatureCelsius;

    /** Fréquence cardiaque (bpm). */
    private Integer heartRateBpm;

    /** Fréquence respiratoire (cycles/min). */
    private Integer respiratoryRateCpm;

    /** Pression artérielle systolique (mmHg). */
    private Integer bloodPressureSystolic;

    /** Pression artérielle diastolique (mmHg). */
    private Integer bloodPressureDiastolic;

    /** Saturation en oxygène SpO2 (%). */
    @Column(precision = 4, scale = 1)
    private BigDecimal oxygenSaturationPercent;

    /** Glycémie capillaire (mmol/L). */
    @Column(precision = 5, scale = 2)
    private BigDecimal bloodGlucoseMmolL;

    /** Poids en kg. */
    @Column(precision = 5, scale = 2)
    private BigDecimal weightKg;

    /** Taille en cm. */
    @Column(precision = 5, scale = 1)
    private BigDecimal heightCm;

    /** Indice de masse corporelle (calculé). */
    @Column(precision = 4, scale = 1)
    private BigDecimal bmi;

    /** Douleur — échelle numérique 0-10. */
    private Integer painScore;

    // ─── Contexte ─────────────────────────────────────────────────────────────

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    /** Identifiant du soignant ayant enregistré les constantes. */
    @Column(length = 50)
    private String recordedBy;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─── Calcul automatique du BMI ────────────────────────────────────────────

    @PrePersist
    @PreUpdate
    public void computeBmi() {
        if (weightKg != null && heightCm != null && heightCm.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightM = heightCm.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
            this.bmi = weightKg.divide(heightM.multiply(heightM), 1, java.math.RoundingMode.HALF_UP);
        }
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
