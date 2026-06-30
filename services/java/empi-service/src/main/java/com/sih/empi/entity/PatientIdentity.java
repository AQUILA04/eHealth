package com.sih.empi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité principale de l'EMPI (Enterprise Master Patient Index).
 *
 * <p>Représente l'identité globale d'un patient dans le hub central.
 * En mode mock (développement), cette entité est persistée en H2 in-memory.
 * En production, elle sera stockée dans PostgreSQL avec chiffrement des PII.
 */
@Entity
@Table(
    name = "patient_identity",
    indexes = {
        @Index(name = "idx_pi_national_id", columnList = "nationalId"),
        @Index(name = "idx_pi_mrn", columnList = "mrn"),
        @Index(name = "idx_pi_global_uuid", columnList = "globalUuid", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant universel unique généré par le système. */
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String globalUuid;

    /** Numéro de dossier médical local (Medical Record Number). */
    @Column(length = 50)
    private String mrn;

    /** Identifiant national (carte d'identité, passeport, etc.). */
    @Column(length = 100)
    private String nationalId;

    // ─── Données démographiques ───────────────────────────────────────────────

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Le nom de famille est obligatoire")
    @Column(nullable = false, length = 100)
    private String lastName;

    @NotNull(message = "La date de naissance est obligatoire")
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

    @Column(length = 10)
    private String postalCode;

    @Column(length = 100)
    private String nationality;

    // ─── Métadonnées système ──────────────────────────────────────────────────

    /**
     * Système source d'identité.
     * MOCK = identité créée localement en mode développement.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SourceSystem sourceSystem = SourceSystem.MOCK;

    /** Indicateur de doublon potentiel détecté par l'algorithme de déduplication. */
    @Column(nullable = false)
    @Builder.Default
    private boolean duplicateSuspected = false;

    /** Score de confiance de l'identité (1.0 = certitude absolue). */
    @Column(nullable = false)
    @Builder.Default
    private double confidenceScore = 1.0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ─── Enums internes ───────────────────────────────────────────────────────

    public enum Gender {
        MALE, FEMALE, OTHER, UNKNOWN
    }

    public enum SourceSystem {
        MOCK, LOCAL_V1, MOSIP
    }

    // ─── Méthodes utilitaires ─────────────────────────────────────────────────

    @PrePersist
    public void prePersist() {
        if (this.globalUuid == null) {
            this.globalUuid = UUID.randomUUID().toString();
        }
        if (this.mrn == null) {
            // MRN auto-généré en mode mock : MRN-YYYYMMDD-XXXX
            this.mrn = "MRN-" + LocalDate.now().toString().replace("-", "")
                       + "-" + String.format("%04d", (int)(Math.random() * 9999));
        }
    }

    /** Retourne le nom complet du patient. */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
