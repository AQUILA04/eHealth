package com.sih.gap.repository;

import com.sih.gap.entity.Appointment;
import com.sih.gap.entity.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.scheduledTime BETWEEN :start AND :end ORDER BY a.scheduledTime")
    List<Appointment> findByScheduledTimeBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT a FROM Appointment a WHERE a.practitionerId = :practitionerId " +
           "AND a.scheduledTime BETWEEN :start AND :end ORDER BY a.scheduledTime")
    List<Appointment> findByPractitionerAndPeriod(
        @Param("practitionerId") String practitionerId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    List<Appointment> findByStatus(AppointmentStatus status);
}
