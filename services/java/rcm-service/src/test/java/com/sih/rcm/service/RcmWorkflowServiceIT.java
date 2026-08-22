package com.sih.rcm.service;

import static org.junit.jupiter.api.Assertions.*;
import com.sih.rcm.dto.RcmDtos.*;
import com.sih.shared.tenant.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class RcmWorkflowServiceIT {
  @Autowired RcmWorkflowService service;
  @AfterEach void cleanup() { TenantContext.clear(); }
  private CreateInvoiceRequest request() { return new CreateInvoiceRequest("PAT-001", 12L, "XOF", "ASSURANCE", "Assureur Santé", new BigDecimal("80"), List.of(new InvoiceLineRequest("CONSULT", "Consultation spécialisée", BigDecimal.ONE, new BigDecimal("10000")))); }
  @Test void appliesPaymentAndInsuranceWorkflowWithinTenant() {
    TenantContext.setCurrentTenant("hospital-a"); var invoice = service.createInvoice(request()); assertEquals(new BigDecimal("8000.00"), invoice.insurerAmount()); invoice = service.issueInvoice(invoice.id()); assertEquals("ISSUED", invoice.status()); var claim = service.createClaim(new CreateClaimRequest(invoice.id(), "Assureur Santé", "POL-01")); claim = service.submitClaim(claim.id()); assertEquals("SUBMITTED", claim.status()); claim = service.adjudicateClaim(claim.id(), new AdjudicateClaimRequest("APPROVED", new BigDecimal("8000"), null)); assertEquals("APPROVED", claim.status()); var payment = service.recordPayment(invoice.id(), new RecordPaymentRequest(new BigDecimal("10000"), "CASH", "REC-1", "Caissier")); assertEquals(new BigDecimal("10000"), payment.amount()); assertEquals("PAID", service.listInvoices().getFirst().status());
  }
  @Test void preventsAccessAcrossTenants() {
    TenantContext.setCurrentTenant("hospital-a"); var invoice = service.createInvoice(request()); TenantContext.setCurrentTenant("hospital-b"); assertTrue(service.listInvoices().isEmpty()); assertThrows(ResponseStatusException.class, () -> service.issueInvoice(invoice.id()));
  }
}
