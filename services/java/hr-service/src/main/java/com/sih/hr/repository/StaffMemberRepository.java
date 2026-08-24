package com.sih.hr.repository;
import com.sih.hr.entity.StaffMember; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface StaffMemberRepository extends JpaRepository<StaffMember,Long> { List<StaffMember> findByTenantIdOrderByLastNameAsc(String tenantId); Optional<StaffMember> findByIdAndTenantId(Long id,String tenantId); }
