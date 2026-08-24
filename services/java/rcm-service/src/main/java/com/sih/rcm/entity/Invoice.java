package com.sih.rcm.entity;

import com.sih.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "rcm_invoice", uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "invoiceNumber"}))
@Getter @Setter
public class Invoice extends TenantScopedEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false) private String invoiceNumber;
  @Column(nullable = false) private String patientRef;
  private Long clinicalEncounterId;
  @Column(nullable = false) private String currency = "XOF";
  @Column(nullable = false) private String payerType = "PATIENT";
  private String insurerName;
  @Column(nullable = false) private String status = "DRAFT";
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal totalAmount = BigDecimal.ZERO;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal insurerAmount = BigDecimal.ZERO;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal patientAmount = BigDecimal.ZERO;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal outstandingAmount = BigDecimal.ZERO;
  @Column(nullable = false) private Instant createdAt = Instant.now();
  private Instant issuedAt;
  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true) private List<InvoiceLine> lines = new ArrayList<>();
}
