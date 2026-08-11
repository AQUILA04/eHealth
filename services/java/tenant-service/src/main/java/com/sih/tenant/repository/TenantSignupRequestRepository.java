package com.sih.tenant.repository;

import com.sih.tenant.entity.SignupRequestStatus;
import com.sih.tenant.entity.TenantSignupRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantSignupRequestRepository extends JpaRepository<TenantSignupRequest, String> {
    List<TenantSignupRequest> findByStatusOrderByCreatedAtDesc(SignupRequestStatus status);
    List<TenantSignupRequest> findAllByOrderByCreatedAtDesc();
    boolean existsByAdminEmailIgnoreCaseAndStatus(String adminEmail, SignupRequestStatus status);
}
