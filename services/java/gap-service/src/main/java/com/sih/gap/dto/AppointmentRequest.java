package com.sih.gap.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "L'identifiant du patient est obligatoire")
    private Long patientId;

    @NotNull(message = "La date et heure du rendez-vous sont obligatoires")
    @Future(message = "Le rendez-vous doit être dans le futur")
    private LocalDateTime scheduledTime;

    @Min(value = 5, message = "La durée minimale est de 5 minutes")
    @Builder.Default
    private int durationMinutes = 30;

    private String specialty;
    private String practitionerName;
    private String practitionerId;
    private String room;
    private String reason;
    private String notes;
}
