package com.sih.pharmacy.entity;
import com.sih.shared.tenant.TenantScopedEntity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity @Table(name = "inventory_lots", uniqueConstraints = @UniqueConstraint(name = "uk_inventory_lot_tenant_product_lot", columnNames = {"tenantId", "product_id", "lotNumber"}), indexes = @Index(name = "idx_inventory_lot_tenant", columnList = "tenantId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryLot extends TenantScopedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id") private MedicationProduct product;
    @Column(nullable = false) private String lotNumber;
    @Column(nullable = false) private Integer quantityOnHand;
    @Column(nullable = false) private LocalDate expiryDate;
    @Column(nullable = false) private String storageLocation;
    private String supplier;
}
