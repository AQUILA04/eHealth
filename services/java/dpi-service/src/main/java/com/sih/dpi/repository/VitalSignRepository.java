package com.sih.dpi.repository;

import com.sih.dpi.entity.VitalSign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VitalSignRepository extends JpaRepository<VitalSign, Long> {

    List<VitalSign> findByClinicalEncounterIdOrderByRecordedAtDesc(Long encounterId);
}
