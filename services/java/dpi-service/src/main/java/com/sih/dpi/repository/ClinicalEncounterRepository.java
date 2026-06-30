package com.sih.dpi.repository;

import com.sih.dpi.entity.ClinicalEncounter;
import com.sih.dpi.entity.ClinicalEncounter.EncounterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicalEncounterRepository extends JpaRepository<ClinicalEncounter, Long> {

    Optional<ClinicalEncounter> findByGapEncounterId(Long gapEncounterId);

    List<ClinicalEncounter> findByPatientRef(String patientRef);

    List<ClinicalEncounter> findByPatientRefAndStatus(String patientRef, EncounterStatus status);

    List<ClinicalEncounter> findByStatus(EncounterStatus status);

    List<ClinicalEncounter> findByEmpiGlobalUuid(String empiGlobalUuid);
}
