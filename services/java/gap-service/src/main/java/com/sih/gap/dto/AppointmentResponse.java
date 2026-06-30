package com.sih.gap.dto;

import com.sih.gap.entity.Appointment.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    private Long patientId;
    private String patientFullName;
    private String patientMrn;

    private LocalDateTime scheduledTime;
    private int durationMinutes;
    private String specialty;
    private String practitionerName;
    private String practitionerId;
    private String room;
    private String reason;
    private AppointmentStatus status;
    private String notes;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
