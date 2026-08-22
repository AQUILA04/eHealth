package com.sih.lis.repository;
import com.sih.lis.entity.LaboratoryOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LaboratoryOrderRepository extends JpaRepository<LaboratoryOrder, Long> {
    List<LaboratoryOrder> findByStatusOrderByOrderedAtAsc(LaboratoryOrder.Status status);
    List<LaboratoryOrder> findByPatientRefOrderByOrderedAtDesc(String patientRef);
}
