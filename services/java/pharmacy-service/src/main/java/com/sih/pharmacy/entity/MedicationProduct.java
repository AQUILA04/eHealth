package com.sih.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "medication_products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicationProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String sku;
    @Column(nullable = false) private String name;
    private String genericName;
    private String atcCode;
    @Column(nullable = false) private String unit;
    @Column(nullable = false) private Integer minimumStock;
    private boolean active;
}
