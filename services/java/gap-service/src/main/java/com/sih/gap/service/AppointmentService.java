package com.sih.gap.service;

import com.sih.gap.dto.AppointmentRequest;
import com.sih.gap.dto.AppointmentResponse;
import com.sih.gap.entity.Appointment;
import com.sih.gap.entity.Appointment.AppointmentStatus;
import com.sih.gap.entity.Patient;
import com.sih.gap.repository.AppointmentRepository;
import com.sih.gap.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service de gestion des rendez-vous (Scheduler — Module I, Section 2.2).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final com.sih.gap.client.QuotaClient quotaClient;

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        quotaClient.assertAndRecordUsage("appointments.create");

        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Patient introuvable avec l'id: " + request.getPatientId()));

        Appointment appointment = Appointment.builder()
            .patient(patient)
            .scheduledTime(request.getScheduledTime())
            .durationMinutes(request.getDurationMinutes())
            .specialty(request.getSpecialty())
            .practitionerName(request.getPractitionerName())
            .practitionerId(request.getPractitionerId())
            .room(request.getRoom())
            .reason(request.getReason())
            .notes(request.getNotes())
            .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("GAP: Rendez-vous créé id={} pour patient={}", saved.getId(), patient.getLocalMrn());
        return toResponse(saved);
    }

    public List<AppointmentResponse> findByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public Optional<AppointmentResponse> findById(Long id) {
        return appointmentRepository.findById(id).map(this::toResponse);
    }

    public List<AppointmentResponse> findByPeriod(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByScheduledTimeBetween(start, end).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public Optional<AppointmentResponse> updateStatus(Long id, AppointmentStatus newStatus,
                                                        String cancellationReason) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setStatus(newStatus);
            if (cancellationReason != null) {
                appointment.setCancellationReason(cancellationReason);
            }
            log.info("GAP: Rendez-vous id={} — statut changé en {}", id, newStatus);
            return toResponse(appointmentRepository.save(appointment));
        });
    }

    @Transactional
    public boolean cancelAppointment(Long id, String reason) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setCancellationReason(reason);
            appointmentRepository.save(appointment);
            return true;
        }).orElse(false);
    }

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
            .id(a.getId())
            .patientId(a.getPatient().getId())
            .patientFullName(a.getPatient().getFullName())
            .patientMrn(a.getPatient().getLocalMrn())
            .scheduledTime(a.getScheduledTime())
            .durationMinutes(a.getDurationMinutes())
            .specialty(a.getSpecialty())
            .practitionerName(a.getPractitionerName())
            .practitionerId(a.getPractitionerId())
            .room(a.getRoom())
            .reason(a.getReason())
            .status(a.getStatus())
            .notes(a.getNotes())
            .cancellationReason(a.getCancellationReason())
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }
}
