package com.sih.hr.repository;
import com.sih.hr.entity.Credential; import java.time.LocalDate; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface CredentialRepository extends JpaRepository<Credential,Long> { List<Credential> findByTenantIdOrderByExpiresOnAsc(String tenantId); Optional<Credential> findByIdAndTenantId(Long id,String tenantId); boolean existsByStaffIdAndTenantIdAndExpiresOnBefore(Long staffId,String tenantId,LocalDate date); }
