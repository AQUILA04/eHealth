package com.sih.lis.entity;

import com.sih.shared.tenant.TenantScopedEntity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "blood_units", uniqueConstraints = @UniqueConstraint(name = "uk_blood_unit_tenant_donation", columnNames = {"tenantId", "donationCode"}), indexes = @Index(name = "idx_blood_unit_tenant", columnList = "tenantId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BloodUnit extends TenantScopedEntity {
    public enum AboGroup { A, B, AB, O }
    public enum Rhesus { POSITIVE, NEGATIVE }
    public enum Component { RED_CELLS, PLASMA, PLATELETS }
    public enum Status { AVAILABLE, RESERVED, ISSUED, TRANSFUSED, EXPIRED, DISCARDED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String donationCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AboGroup aboGroup;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Rhesus rhesus;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Component component;
    @Column(nullable = false) private LocalDate collectedOn;
    @Column(nullable = false) private LocalDate expiresOn;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    private String storageLocation;
}
