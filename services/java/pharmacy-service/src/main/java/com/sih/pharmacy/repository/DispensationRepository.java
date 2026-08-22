package com.sih.pharmacy.repository;
import com.sih.pharmacy.entity.Dispensation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DispensationRepository extends JpaRepository<Dispensation, Long> { List<Dispensation> findByPatientRefOrderByValidatedAtDesc(String patientRef); }
