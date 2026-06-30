package com.sih.dpi.repository;

import com.sih.dpi.entity.LabOrder;
import com.sih.dpi.entity.LabOrder.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    List<LabOrder> findByClinicalEncounterId(Long encounterId);

    List<LabOrder> findByClinicalEncounterIdAndStatus(Long encounterId, OrderStatus status);

    List<LabOrder> findByStatus(OrderStatus status);
}
