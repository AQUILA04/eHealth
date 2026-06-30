package com.sih.empi.repository;

import com.sih.empi.entity.PatientIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour l'index maître patient (EMPI).
 */
@Repository
public interface PatientIdentityRepository extends JpaRepository<PatientIdentity, Long> {

    Optional<PatientIdentity> findByGlobalUuid(String globalUuid);

    Optional<PatientIdentity> findByMrn(String mrn);

    Optional<PatientIdentity> findByNationalId(String nationalId);

    List<PatientIdentity> findByActiveTrue();

    /**
     * Recherche par nom et prénom (insensible à la casse) pour la déduplication.
     */
    @Query("SELECT p FROM PatientIdentity p WHERE " +
           "LOWER(p.firstName) = LOWER(:firstName) AND " +
           "LOWER(p.lastName) = LOWER(:lastName) AND " +
           "p.dateOfBirth = :dob AND p.active = true")
    List<PatientIdentity> findExactDuplicates(
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("dob") LocalDate dob
    );

    /**
     * Recherche phonétique / partielle pour la déduplication probabiliste.
     * Utilise LIKE pour le mock ; en production, remplacer par pg_trgm ou Elasticsearch.
     */
    @Query("SELECT p FROM PatientIdentity p WHERE " +
           "(LOWER(p.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')) OR " +
           " LOWER(p.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "p.active = true")
    List<PatientIdentity> findProbabilisticCandidates(
        @Param("firstName") String firstName,
        @Param("lastName") String lastName
    );

    /**
     * Recherche full-text simplifiée (nom, prénom, MRN, nationalId).
     */
    @Query("SELECT p FROM PatientIdentity p WHERE p.active = true AND (" +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.mrn LIKE CONCAT('%', :query, '%') OR " +
           "p.nationalId LIKE CONCAT('%', :query, '%'))")
    List<PatientIdentity> search(@Param("query") String query);
}
