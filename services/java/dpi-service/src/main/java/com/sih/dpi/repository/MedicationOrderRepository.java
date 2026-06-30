package com.sih.dpi.repository;

import com.sih.dpi.entity.MedicationOrder;
import com.sih.dpi.entity.MedicationOrder.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationOrderRepository extends JpaRepository<MedicationOrder, Long> {

    List<MedicationOrder> findByClinicalEncounterId(Long encounterId);

    List<MedicationOrder> findByClinicalEncounterIdAndStatus(Long encounterId, OrderStatus status);
}
