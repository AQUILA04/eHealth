package com.sih.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity @Table(name = "dispensations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Dispensation {
    public enum Status { VALIDATED, DISPENSED, CANCELLED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long clinicalEncounterId;
    @Column(nullable = false) private String patientRef;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private MedicationProduct product;
    @ManyToOne(fetch = FetchType.LAZY) private InventoryLot lot;
    @Column(nullable = false) private Integer quantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    @Column(nullable = false) private String pharmacist;
    private String clinicalPrescriptionRef;
    private LocalDateTime validatedAt;
    private LocalDateTime dispensedAt;
}
