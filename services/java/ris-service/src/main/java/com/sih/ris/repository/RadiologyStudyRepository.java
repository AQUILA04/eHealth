package com.sih.ris.repository;
import com.sih.ris.entity.RadiologyStudy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RadiologyStudyRepository extends JpaRepository<RadiologyStudy, Long> {
    List<RadiologyStudy> findByStatusOrderByScheduledAtAsc(RadiologyStudy.Status status);
    List<RadiologyStudy> findByPatientRefOrderByRequestedAtDesc(String patientRef);
}
