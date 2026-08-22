package com.sih.rcm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "rcm_invoice_line") @Getter @Setter
public class InvoiceLine {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "invoice_id") private Invoice invoice;
  @Column(nullable = false) private String serviceCode;
  @Column(nullable = false) private String description;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal quantity;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal unitPrice;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal lineTotal;
}
