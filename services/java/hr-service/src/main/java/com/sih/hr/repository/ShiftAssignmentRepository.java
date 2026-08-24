package com.sih.hr.repository;
import com.sih.hr.entity.ShiftAssignment; import java.time.OffsetDateTime; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment,Long> { List<ShiftAssignment> findByTenantIdOrderByStartsAtAsc(String tenantId); Optional<ShiftAssignment> findByIdAndTenantId(Long id,String tenantId); boolean existsByStaffIdAndTenantIdAndStartsAtLessThanAndEndsAtGreaterThan(Long staffId,String tenantId,OffsetDateTime end,OffsetDateTime start); }
