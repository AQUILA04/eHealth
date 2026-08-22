package com.sih.lis.repository;
import com.sih.lis.entity.TransfusionRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TransfusionRequestRepository extends JpaRepository<TransfusionRequest, Long> {
    List<TransfusionRequest> findByPatientRefOrderByRequestedAtDesc(String patientRef);
    List<TransfusionRequest> findByStatusOrderByRequestedAtAsc(TransfusionRequest.Status status);
}
