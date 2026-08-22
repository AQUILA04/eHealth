package com.sih.rcm.entity;

import com.sih.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "rcm_insurance_claim", uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "claimNumber"}))
@Getter @Setter
public class InsuranceClaim extends TenantScopedEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false) private String claimNumber;
  @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "invoice_id") private Invoice invoice;
  @Column(nullable = false) private String insurerName;
  @Column(nullable = false) private String policyNumber;
  @Column(nullable = false) private String status = "DRAFT";
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal requestedAmount;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal approvedAmount = BigDecimal.ZERO;
  private String denialReason;
  @Column(nullable = false) private Instant createdAt = Instant.now();
  private Instant submittedAt;
  private Instant adjudicatedAt;
}
