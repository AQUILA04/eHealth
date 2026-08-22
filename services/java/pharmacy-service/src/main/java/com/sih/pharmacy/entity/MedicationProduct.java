package com.sih.pharmacy.entity;
import com.sih.shared.tenant.TenantScopedEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "medication_products", uniqueConstraints = @UniqueConstraint(name = "uk_medication_product_tenant_sku", columnNames = {"tenantId", "sku"}), indexes = @Index(name = "idx_medication_product_tenant", columnList = "tenantId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicationProduct extends TenantScopedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String sku;
    @Column(nullable = false) private String name;
    private String genericName;
    private String atcCode;
    @Column(nullable = false) private String unit;
    @Column(nullable = false) private Integer minimumStock;
    private boolean active;
}
