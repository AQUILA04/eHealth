package com.sih.lis.dto;

import com.sih.lis.entity.BloodUnit;
import com.sih.lis.entity.TransfusionRequest;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class BloodBankDtos {
    private BloodBankDtos() { }
    public record ReceiveBloodUnitRequest(@NotBlank String donationCode, @NotNull BloodUnit.AboGroup aboGroup,
        @NotNull BloodUnit.Rhesus rhesus, @NotNull BloodUnit.Component component, @NotNull LocalDate collectedOn,
        @NotNull @Future LocalDate expiresOn, String storageLocation) { }
    public record CreateTransfusionRequest(@NotNull Long clinicalEncounterId, @NotBlank String patientRef,
        @NotNull BloodUnit.AboGroup recipientAboGroup, @NotNull BloodUnit.Rhesus recipientRhesus,
        @NotNull BloodUnit.Component component, @NotBlank String requestedBy) { }
    public record CrossmatchRequest(@NotBlank String validatedBy) { }
    public record IssueRequest(@NotBlank String issuedBy) { }
    public record CompleteTransfusionRequest(@NotBlank String completedBy) { }
    public record ReportReactionRequest(@NotBlank String reactionDescription) { }
    public record BloodUnitResponse(Long id, String donationCode, BloodUnit.AboGroup aboGroup, BloodUnit.Rhesus rhesus,
        BloodUnit.Component component, LocalDate collectedOn, LocalDate expiresOn, BloodUnit.Status status,
        String storageLocation, boolean expiringSoon) { }
    public record TransfusionResponse(Long id, Long clinicalEncounterId, String patientRef, BloodUnit.AboGroup recipientAboGroup,
        BloodUnit.Rhesus recipientRhesus, BloodUnit.Component component, Long bloodUnitId, String donationCode,
        BloodUnit.AboGroup donorAboGroup, BloodUnit.Rhesus donorRhesus, TransfusionRequest.Status status, String requestedBy,
        String crossmatchValidatedBy, String issuedBy, String completedBy, LocalDateTime requestedAt,
        LocalDateTime crossmatchValidatedAt, LocalDateTime issuedAt, LocalDateTime completedAt, String reactionDescription,
        LocalDateTime reactionReportedAt) { }
}
