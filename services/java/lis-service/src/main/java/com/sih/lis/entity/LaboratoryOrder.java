package com.sih.lis.entity;

import com.sih.shared.tenant.TenantScopedEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "laboratory_orders", uniqueConstraints = @UniqueConstraint(name = "uk_laboratory_order_tenant_barcode", columnNames = {"tenantId", "barcode"}), indexes = @Index(name = "idx_laboratory_order_tenant", columnList = "tenantId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LaboratoryOrder extends TenantScopedEntity {
    public enum Priority { ROUTINE, URGENT, STAT }
    public enum Status { ORDERED, COLLECTED, RECEIVED, IN_ANALYSIS, TECHNICALLY_VALIDATED, BIOLOGICALLY_VALIDATED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long clinicalEncounterId;
    @Column(nullable = false, length = 100) private String patientRef;
    @Column(nullable = false, length = 160) private String examName;
    @Column(length = 50) private String examCode;
    @Column(nullable = false, length = 60) private String sampleType;
    @Column(length = 80) private String barcode;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Priority priority;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    private String orderedBy;
    private String collectedBy;
    private String receivedBy;
    private String validatedBy;
    private LocalDateTime orderedAt;
    private LocalDateTime collectedAt;
    private LocalDateTime receivedAt;
    private LocalDateTime validatedAt;
    private LocalDateTime criticalNotifiedAt;
    private String criticalNotifiedTo;
    @OneToMany(mappedBy = "laboratoryOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<LaboratoryResult> results = new ArrayList<>();
}
