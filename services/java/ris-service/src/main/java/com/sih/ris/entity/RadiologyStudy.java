package com.sih.ris.entity;
import com.sih.shared.tenant.TenantScopedEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "radiology_studies", indexes = @Index(name = "idx_radiology_study_tenant", columnList = "tenantId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RadiologyStudy extends TenantScopedEntity {
    public enum Modality { XR, CT, MRI, US, NM, MAMMO, OTHER }
    public enum Priority { ROUTINE, URGENT, STAT }
    public enum Status { REQUESTED, SCHEDULED, CHECKED_IN, IN_PROGRESS, COMPLETED, REPORTED, CANCELLED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long clinicalEncounterId;
    @Column(nullable = false) private String patientRef;
    @Column(nullable = false) private String procedureName;
    private String procedureCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Modality modality;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Priority priority;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    private String requestedBy;
    private String assignedRadiologist;
    private String assignedTechnologist;
    private String pacsStudyUid;
    private String reportText;
    private Double radiationDoseMgy;
    private LocalDateTime requestedAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime performedAt;
    private LocalDateTime reportedAt;
}
