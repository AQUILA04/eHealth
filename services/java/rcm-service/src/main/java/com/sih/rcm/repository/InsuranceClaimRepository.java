package com.sih.rcm.repository;
import com.sih.rcm.entity.InsuranceClaim;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> { List<InsuranceClaim> findByTenantIdOrderByCreatedAtDesc(String tenantId); Optional<InsuranceClaim> findByIdAndTenantId(Long id, String tenantId); }
