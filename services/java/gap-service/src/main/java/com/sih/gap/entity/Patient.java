package com.sih.gap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Patient locale du GAP (Gestion Administrative du Patient).
 *
 * <p>Représente le dossier administratif local du patient dans l'établissement.
 * Le champ {@code empiGlobalUuid} est la clé de liaison avec l'EMPI central.
 *
 * <p>Conforme au Module I — Section 2.1 de la spécification SIH.
 */
@Entity
@Table(
    name = "patient",
    indexes = {
        @Index(name = "idx_patient_empi_uuid", columnList = "empiGlobalUuid"),
        @Index(name = "idx_patient_local_mrn", columnList = "localMrn", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * UUID global de l'EMPI — clé de liaison avec l'index maître patient.
     * Peut être null si le patient n'a pas encore été synchronisé avec l'EMPI.
     */
    @Column(length = 36)
    private String empiGlobalUuid;

    /** Numéro de dossier médical local à l'établissement. */
    @Column(nullable = false, unique = true, length = 50)
    private String localMrn;

    // ─── Données démographiques (cache local de l'EMPI) ──────────────────────

    @NotBlank
    @Column(nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String lastName;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String nationality;

    // ─── Données administratives ──────────────────────────────────────────────

    /** Couverture financière principale. */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private FinancialCoverage financialCoverage = FinancialCoverage.SELF_PAY;

    /** Numéro de police d'assurance. */
    @Column(length = 100)
    private String insurancePolicyNumber;

    /** Nom de la compagnie d'assurance. */
    @Column(length = 150)
    private String insuranceCompany;

    /** Contact d'urgence — nom. */
    @Column(length = 200)
    private String emergencyContactName;

    /** Contact d'urgence — téléphone. */
    @Column(length = 20)
    private String emergencyContactPhone;

    // ─── Statut ───────────────────────────────────────────────────────────────

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // ─── Relations ────────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Encounter> encounters = new ArrayList<>();

    // ─── Métadonnées ──────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ─── Enums ────────────────────────────────────────────────────────────────

    public enum Gender {
        MALE, FEMALE, OTHER, UNKNOWN
    }

    public enum FinancialCoverage {
        SELF_PAY, INSURANCE, STATE, MUTUAL, CORPORATE
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────────

    @PrePersist
    public void prePersist() {
        if (this.localMrn == null) {
            this.localMrn = "GAP-" + LocalDate.now().toString().replace("-", "")
                            + "-" + String.format("%05d", (long)(Math.random() * 99999));
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
