package com.sih.rcm.service;

import com.sih.rcm.dto.RcmDtos.*;
import com.sih.rcm.entity.*;
import com.sih.rcm.repository.*;
import com.sih.shared.tenant.TenantContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class RcmWorkflowService {
  private final InvoiceRepository invoices; private final PaymentRepository payments; private final InsuranceClaimRepository claims;
  public RcmWorkflowService(InvoiceRepository invoices, PaymentRepository payments, InsuranceClaimRepository claims) { this.invoices = invoices; this.payments = payments; this.claims = claims; }
  private String tenant() { return TenantContext.requireCurrentTenant(); }
  private Invoice invoice(Long id) { return invoices.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture introuvable")); }
  private InsuranceClaim claim(Long id) { return claims.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dossier assureur introuvable")); }
  public List<InvoiceResponse> listInvoices() { return invoices.findByTenantIdOrderByCreatedAtDesc(tenant()).stream().map(this::toInvoice).toList(); }
  public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
    Invoice inv = new Invoice(); inv.setInvoiceNumber("FAC-" + Instant.now().toEpochMilli()); inv.setPatientRef(request.patientRef()); inv.setClinicalEncounterId(request.clinicalEncounterId()); inv.setCurrency(request.currency() == null || request.currency().isBlank() ? "XOF" : request.currency()); inv.setPayerType(request.payerType()); inv.setInsurerName(request.insurerName());
    BigDecimal total = BigDecimal.ZERO;
    for (InvoiceLineRequest line : request.lines()) { InvoiceLine entity = new InvoiceLine(); entity.setInvoice(inv); entity.setServiceCode(line.serviceCode()); entity.setDescription(line.description()); entity.setQuantity(line.quantity()); entity.setUnitPrice(line.unitPrice()); entity.setLineTotal(line.quantity().multiply(line.unitPrice()).setScale(2, RoundingMode.HALF_UP)); total = total.add(entity.getLineTotal()); inv.getLines().add(entity); }
    BigDecimal percent = request.coveragePercent() == null ? BigDecimal.ZERO : request.coveragePercent(); BigDecimal insurer = total.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP); inv.setTotalAmount(total); inv.setInsurerAmount(insurer); inv.setPatientAmount(total.subtract(insurer)); inv.setOutstandingAmount(total); return toInvoice(invoices.save(inv));
  }
  public InvoiceResponse issueInvoice(Long id) { Invoice inv = invoice(id); if (!"DRAFT".equals(inv.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Seule une facture brouillon peut être émise"); inv.setStatus("ISSUED"); inv.setIssuedAt(Instant.now()); return toInvoice(inv); }
  public PaymentResponse recordPayment(Long id, RecordPaymentRequest request) { Invoice inv = invoice(id); if (!("ISSUED".equals(inv.getStatus()) || "PARTIALLY_PAID".equals(inv.getStatus()))) throw new ResponseStatusException(HttpStatus.CONFLICT, "La facture doit être émise avant encaissement"); if (request.amount().compareTo(inv.getOutstandingAmount()) > 0) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le paiement dépasse le solde restant"); Payment p = new Payment(); p.setInvoice(inv); p.setAmount(request.amount()); p.setMethod(request.method()); p.setReference(request.reference()); p.setReceivedBy(request.receivedBy()); payments.save(p); BigDecimal remaining = inv.getOutstandingAmount().subtract(request.amount()); inv.setOutstandingAmount(remaining); inv.setStatus(remaining.signum() == 0 ? "PAID" : "PARTIALLY_PAID"); return toPayment(p); }
  public List<PaymentResponse> listPayments(Long invoiceId) { invoice(invoiceId); return payments.findByInvoiceIdAndTenantIdOrderByReceivedAtDesc(invoiceId, tenant()).stream().map(this::toPayment).toList(); }
  public List<ClaimResponse> listClaims() { return claims.findByTenantIdOrderByCreatedAtDesc(tenant()).stream().map(this::toClaim).toList(); }
  public ClaimResponse createClaim(CreateClaimRequest request) { Invoice inv = invoice(request.invoiceId()); if (!("ISSUED".equals(inv.getStatus()) || "PARTIALLY_PAID".equals(inv.getStatus()) || "PAID".equals(inv.getStatus()))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Émettre la facture avant de créer un dossier assureur"); if (inv.getInsurerAmount().signum() <= 0) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Aucune part assureur à réclamer"); InsuranceClaim c = new InsuranceClaim(); c.setClaimNumber("REC-" + Instant.now().toEpochMilli()); c.setInvoice(inv); c.setInsurerName(request.insurerName()); c.setPolicyNumber(request.policyNumber()); c.setRequestedAmount(inv.getInsurerAmount()); return toClaim(claims.save(c)); }
  public ClaimResponse submitClaim(Long id) { InsuranceClaim c = claim(id); if (!"DRAFT".equals(c.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un dossier brouillon peut être envoyé"); c.setStatus("SUBMITTED"); c.setSubmittedAt(Instant.now()); return toClaim(c); }
  public ClaimResponse adjudicateClaim(Long id, AdjudicateClaimRequest request) { InsuranceClaim c = claim(id); if (!"SUBMITTED".equals(c.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un dossier envoyé peut être instruit"); if (request.approvedAmount().compareTo(c.getRequestedAmount()) > 0) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le montant accepté dépasse le montant réclamé"); if ("DENIED".equals(request.status()) && request.approvedAmount().signum() != 0) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Un refus doit avoir un montant accepté nul"); c.setStatus(request.status()); c.setApprovedAmount(request.approvedAmount()); c.setDenialReason(request.denialReason()); c.setAdjudicatedAt(Instant.now()); return toClaim(c); }
  private InvoiceResponse toInvoice(Invoice i) { return new InvoiceResponse(i.getId(), i.getInvoiceNumber(), i.getPatientRef(), i.getClinicalEncounterId(), i.getCurrency(), i.getPayerType(), i.getInsurerName(), i.getStatus(), i.getTotalAmount(), i.getInsurerAmount(), i.getPatientAmount(), i.getOutstandingAmount(), i.getCreatedAt(), i.getIssuedAt(), i.getLines().stream().map(l -> new InvoiceLineResponse(l.getId(), l.getServiceCode(), l.getDescription(), l.getQuantity(), l.getUnitPrice(), l.getLineTotal())).toList()); }
  private PaymentResponse toPayment(Payment p) { return new PaymentResponse(p.getId(), p.getInvoice().getId(), p.getAmount(), p.getMethod(), p.getReference(), p.getReceivedAt(), p.getReceivedBy()); }
  private ClaimResponse toClaim(InsuranceClaim c) { return new ClaimResponse(c.getId(), c.getClaimNumber(), c.getInvoice().getId(), c.getInsurerName(), c.getPolicyNumber(), c.getStatus(), c.getRequestedAmount(), c.getApprovedAmount(), c.getDenialReason(), c.getCreatedAt(), c.getSubmittedAt(), c.getAdjudicatedAt()); }
}
