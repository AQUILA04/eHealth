package com.sih.dpi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dossier clinique d'un épisode de soins (ClinicalEncounter / EMR).
 *
 * <p>Lié à un Encounter administratif du GAP via {@code gapEncounterId}.
 * Contient les observations cliniques, prescriptions et notes médicales.
 *
 * <p>Conforme au Module II — Section 3.1 de la spécification SIH.
 */
@Entity
@Table(
    name = "clinical_encounter",
    indexes = {
        @Index(name = "idx_ce_gap_encounter", columnList = "gapEncounterId"),
        @Index(name = "idx_ce_patient_ref", columnList = "patientRef"),
        @Index(name = "idx_ce_status", columnList = "status"),
        @Index(name = "idx_ce_tenant", columnList = "tenantId")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalEncounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    /**
     * Référence vers l'Encounter administratif du GAP service.
     * Clé de liaison inter-services.
     */
    @Column(nullable = false)
    private Long gapEncounterId;

    /**
     * Référence vers le patient (localMrn du GAP).
     * Dénormalisé pour éviter les appels inter-services en lecture.
     */
    @Column(nullable = false, length = 50)
    private String patientRef;

    /** UUID EMPI du patient (pour l'interopérabilité). */
    @Column(length = 36)
    private String empiGlobalUuid;

    // ─── Type et statut ───────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EncounterType encounterType = EncounterType.OUTPATIENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EncounterStatus status = EncounterStatus.IN_PROGRESS;

    // ─── Informations cliniques ───────────────────────────────────────────────

    /** Motif de consultation (chief complaint). */
    @Column(length = 500)
    private String chiefComplaint;

    /** Anamnèse / histoire de la maladie. */
    @Column(length = 3000)
    private String historyOfPresentIllness;

    /** Antécédents médicaux. */
    @Column(length = 2000)
    private String pastMedicalHistory;

    /** Allergies connues. */
    @Column(length = 1000)
    private String allergies;

    /** Traitements en cours. */
    @Column(length = 1000)
    private String currentMedications;

    // ─── Examen clinique ──────────────────────────────────────────────────────

    /** Notes d'examen physique. */
    @Column(length = 3000)
    private String physicalExamination;

    // ─── Diagnostic ───────────────────────────────────────────────────────────

    /** Diagnostic principal (code CIM-10). */
    @Column(length = 20)
    private String primaryDiagnosisCode;

    /** Libellé du diagnostic principal. */
    @Column(length = 300)
    private String primaryDiagnosisLabel;

    /** Diagnostics secondaires (codes CIM-10, séparés par virgule). */
    @Column(length = 500)
    private String secondaryDiagnosesCodes;

    // ─── Plan de soins ────────────────────────────────────────────────────────

    @Column(length = 3000)
    private String treatmentPlan;

    /** Notes de sortie / résumé clinique. */
    @Column(length = 3000)
    private String clinicalSummary;

    // ─── Praticien ────────────────────────────────────────────────────────────

    @Column(length = 200)
    private String attendingPhysicianName;

    @Column(length = 50)
    private String attendingPhysicianId;

    @Column(length = 100)
    private String specialty;

    // ─── Relations ────────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "clinicalEncounter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VitalSign> vitalSigns = new ArrayList<>();

    @OneToMany(mappedBy = "clinicalEncounter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicationOrder> medicationOrders = new ArrayList<>();

    @OneToMany(mappedBy = "clinicalEncounter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LabOrder> labOrders = new ArrayList<>();

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
        IN_PROGRESS, ON_HOLD, FINISHED, CANCELLED
    }

    @PrePersist
    public void prePersist() {
        if (this.tenantId == null) {
            this.tenantId = com.sih.shared.tenant.TenantContext.getCurrentTenant();
        }
    }
}
