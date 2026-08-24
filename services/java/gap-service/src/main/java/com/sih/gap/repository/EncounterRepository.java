package com.sih.gap.repository;

import com.sih.gap.entity.Encounter;
import com.sih.gap.entity.Encounter.EncounterStatus;
import com.sih.gap.entity.Encounter.EncounterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Long> {

    List<Encounter> findByPatientId(Long patientId);

    List<Encounter> findByPatientIdAndStatus(Long patientId, EncounterStatus status);

    /** Retourne l'encounter actif (IN_PROGRESS) d'un patient, s'il existe. */
    @Query("SELECT e FROM Encounter e WHERE e.patient.id = :patientId " +
           "AND e.status = 'IN_PROGRESS' ORDER BY e.admissionDate DESC")
    Optional<Encounter> findActiveEncounterByPatientId(@Param("patientId") Long patientId);

    /** Tableau de bord des lits — tous les encounters IN_PROGRESS. */
    List<Encounter> findByStatus(EncounterStatus status);

    /** Encounters par service (ward). */
    List<Encounter> findByWardAndStatus(String ward, EncounterStatus status);

    /** Indique si un lit est déjà occupé par un séjour actif. */
    boolean existsByWardAndRoomAndBedNumberAndStatus(String ward, String room, String bedNumber, EncounterStatus status);

    /** Encounters par type. */
    List<Encounter> findByEncounterType(EncounterType type);
}
