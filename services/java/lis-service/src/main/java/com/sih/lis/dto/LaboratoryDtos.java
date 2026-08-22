package com.sih.lis.dto;

import com.sih.lis.entity.LaboratoryOrder;
import com.sih.lis.entity.LaboratoryResult;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public final class LaboratoryDtos {
    private LaboratoryDtos() { }
    public record CreateOrderRequest(@NotNull Long clinicalEncounterId, @NotBlank String patientRef, @NotBlank String examName,
        String examCode, @NotBlank String sampleType, LaboratoryOrder.Priority priority, String orderedBy) { }
    public record CollectSpecimenRequest(@NotBlank String collectedBy, LocalDateTime collectedAt) { }
    public record ReceiveSpecimenRequest(@NotBlank String receivedBy, LocalDateTime receivedAt) { }
    public record CreateResultRequest(@NotBlank String analyteName, String analyteCode, @NotBlank String resultValue,
        String unit, String referenceRange, @NotNull LaboratoryResult.Interpretation interpretation, String technicalValidator) { }
    public record ValidateOrderRequest(@NotBlank String validatedBy) { }
    public record CriticalNotificationRequest(@NotBlank String notifiedTo) { }
    public record ResultResponse(Long id, String analyteName, String analyteCode, String resultValue, String unit,
        String referenceRange, LaboratoryResult.Interpretation interpretation, String technicalValidator, LocalDateTime resultedAt) { }
    public record OrderResponse(Long id, Long clinicalEncounterId, String patientRef, String examName, String examCode,
        String sampleType, String barcode, LaboratoryOrder.Priority priority, LaboratoryOrder.Status status, String orderedBy,
        String collectedBy, String receivedBy, String validatedBy, LocalDateTime orderedAt, LocalDateTime collectedAt,
        LocalDateTime receivedAt, LocalDateTime validatedAt, LocalDateTime criticalNotifiedAt, String criticalNotifiedTo,
        List<ResultResponse> results) { }
}
