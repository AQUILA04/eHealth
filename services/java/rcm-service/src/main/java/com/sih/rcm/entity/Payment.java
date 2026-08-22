package com.sih.rcm.entity;

import com.sih.shared.tenant.TenantScopedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "rcm_payment") @Getter @Setter
public class Payment extends TenantScopedEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "invoice_id") private Invoice invoice;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
  @Column(nullable = false) private String method;
  private String reference;
  @Column(nullable = false) private Instant receivedAt = Instant.now();
  @Column(nullable = false) private String receivedBy;
}
