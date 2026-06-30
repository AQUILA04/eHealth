package com.sih.gap.repository;

import com.sih.gap.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByLocalMrn(String localMrn);

    Optional<Patient> findByEmpiGlobalUuid(String empiGlobalUuid);

    List<Patient> findByActiveTrue();

    @Query("SELECT p FROM Patient p WHERE p.active = true AND (" +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "p.localMrn LIKE CONCAT('%', :q, '%') OR " +
           "p.empiGlobalUuid LIKE CONCAT('%', :q, '%'))")
    List<Patient> search(@Param("q") String query);
}
