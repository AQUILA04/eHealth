package com.sih.rcm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class RcmDtos {
  private RcmDtos() { }
  public record InvoiceLineRequest(@NotBlank String serviceCode, @NotBlank String description, @NotNull @DecimalMin(value = "0.01") BigDecimal quantity, @NotNull @DecimalMin(value = "0.00") BigDecimal unitPrice) { }
  public record CreateInvoiceRequest(@NotBlank String patientRef, Long clinicalEncounterId, String currency, @NotBlank String payerType, String insurerName, @DecimalMin(value = "0.00") @DecimalMax(value = "100.00") BigDecimal coveragePercent, @NotEmpty List<@Valid InvoiceLineRequest> lines) { }
  public record InvoiceLineResponse(Long id, String serviceCode, String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal) { }
  public record InvoiceResponse(Long id, String invoiceNumber, String patientRef, Long clinicalEncounterId, String currency, String payerType, String insurerName, String status, BigDecimal totalAmount, BigDecimal insurerAmount, BigDecimal patientAmount, BigDecimal outstandingAmount, Instant createdAt, Instant issuedAt, List<InvoiceLineResponse> lines) { }
  public record RecordPaymentRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount, @NotBlank String method, String reference, @NotBlank String receivedBy) { }
  public record PaymentResponse(Long id, Long invoiceId, BigDecimal amount, String method, String reference, Instant receivedAt, String receivedBy) { }
  public record CreateClaimRequest(@NotNull Long invoiceId, @NotBlank String insurerName, @NotBlank String policyNumber) { }
  public record AdjudicateClaimRequest(@NotBlank @Pattern(regexp = "APPROVED|PARTIALLY_APPROVED|DENIED") String status, @NotNull @DecimalMin(value = "0.00") BigDecimal approvedAmount, String denialReason) { }
  public record ClaimResponse(Long id, String claimNumber, Long invoiceId, String insurerName, String policyNumber, String status, BigDecimal requestedAmount, BigDecimal approvedAmount, String denialReason, Instant createdAt, Instant submittedAt, Instant adjudicatedAt) { }
}
