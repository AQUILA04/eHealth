package com.sih.pharmacy.dto;

import com.sih.pharmacy.entity.Dispensation;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PharmacyDtos {
    private PharmacyDtos() { }
    public record CreateProductRequest(@NotBlank String sku, @NotBlank String name, String genericName, String atcCode,
        @NotBlank String unit, @NotNull @PositiveOrZero Integer minimumStock) { }
    public record ReceiveLotRequest(@NotNull Long productId, @NotBlank String lotNumber, @NotNull @Positive Integer quantity,
        @NotNull @Future LocalDate expiryDate, @NotBlank String storageLocation, String supplier) { }
    public record CreateDispensationRequest(@NotNull Long clinicalEncounterId, @NotBlank String patientRef, @NotNull Long productId,
        @NotNull @Positive Integer quantity, @NotBlank String pharmacist, String clinicalPrescriptionRef) { }
    public record DispenseRequest(Long lotId) { }
    public record ProductResponse(Long id, String sku, String name, String genericName, String atcCode, String unit,
        Integer minimumStock, Integer quantityOnHand, boolean lowStock, boolean active) { }
    public record LotResponse(Long id, Long productId, String productName, String lotNumber, Integer quantityOnHand,
        LocalDate expiryDate, String storageLocation, String supplier, boolean expiringSoon) { }
    public record DispensationResponse(Long id, Long clinicalEncounterId, String patientRef, Long productId, String productName,
        Long lotId, String lotNumber, Integer quantity, Dispensation.Status status, String pharmacist,
        String clinicalPrescriptionRef, LocalDateTime validatedAt, LocalDateTime dispensedAt) { }
}
