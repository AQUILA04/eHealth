package com.sih.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity @Table(name = "inventory_lots", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "lotNumber"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryLot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id") private MedicationProduct product;
    @Column(nullable = false) private String lotNumber;
    @Column(nullable = false) private Integer quantityOnHand;
    @Column(nullable = false) private LocalDate expiryDate;
    @Column(nullable = false) private String storageLocation;
    private String supplier;
}
