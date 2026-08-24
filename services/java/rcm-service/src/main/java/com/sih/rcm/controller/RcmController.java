package com.sih.rcm.controller;

import com.sih.rcm.dto.RcmDtos.*;
import com.sih.rcm.service.RcmWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/rcm")
public class RcmController {
  private final RcmWorkflowService service; public RcmController(RcmWorkflowService service) { this.service = service; }
  @GetMapping("/invoices") public List<InvoiceResponse> invoices() { return service.listInvoices(); }
  @PostMapping("/invoices") @ResponseStatus(HttpStatus.CREATED) public InvoiceResponse createInvoice(@Valid @RequestBody CreateInvoiceRequest r) { return service.createInvoice(r); }
  @PostMapping("/invoices/{id}/issue") public InvoiceResponse issueInvoice(@PathVariable Long id) { return service.issueInvoice(id); }
  @GetMapping("/invoices/{id}/payments") public List<PaymentResponse> payments(@PathVariable Long id) { return service.listPayments(id); }
  @PostMapping("/invoices/{id}/payments") @ResponseStatus(HttpStatus.CREATED) public PaymentResponse payment(@PathVariable Long id, @Valid @RequestBody RecordPaymentRequest r) { return service.recordPayment(id, r); }
  @GetMapping("/claims") public List<ClaimResponse> claims() { return service.listClaims(); }
  @PostMapping("/claims") @ResponseStatus(HttpStatus.CREATED) public ClaimResponse createClaim(@Valid @RequestBody CreateClaimRequest r) { return service.createClaim(r); }
  @PostMapping("/claims/{id}/submit") public ClaimResponse submitClaim(@PathVariable Long id) { return service.submitClaim(id); }
  @PostMapping("/claims/{id}/adjudicate") public ClaimResponse adjudicateClaim(@PathVariable Long id, @Valid @RequestBody AdjudicateClaimRequest r) { return service.adjudicateClaim(id, r); }
}
