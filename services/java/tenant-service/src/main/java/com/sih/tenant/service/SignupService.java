package com.sih.tenant.service;

import com.sih.tenant.dto.*;
import com.sih.tenant.entity.SignupRequestStatus;
import com.sih.tenant.entity.SubscriptionPlan;
import com.sih.tenant.entity.TenantSignupRequest;
import com.sih.tenant.repository.SubscriptionPlanRepository;
import com.sih.tenant.repository.TenantSignupRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {

    private final TenantSignupRequestRepository signupRepository;
    private final SubscriptionPlanRepository planRepository;
    private final TenantProvisioningService provisioningService;

    @Transactional
    public SignupSubmitResult submit(SignupSubmitRequest request) {
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan introuvable"));

        if (!plan.isActive() || !plan.isPublic()) {
            throw new IllegalArgumentException("Ce plan n'est pas disponible à l'inscription");
        }

        if (signupRepository.existsByAdminEmailIgnoreCaseAndStatus(request.getAdminEmail(), SignupRequestStatus.PENDING)) {
            throw new IllegalArgumentException("Une demande est déjà en cours pour cet email");
        }

        TenantSignupRequest entity = TenantSignupRequest.builder()
                .organizationName(request.getOrganizationName().trim())
                .subdomain(request.getSubdomain())
                .adminEmail(request.getAdminEmail().trim().toLowerCase())
                .adminFirstName(request.getAdminFirstName().trim())
                .adminLastName(request.getAdminLastName().trim())
                .adminPhone(request.getAdminPhone())
                .planId(plan.getId())
                .status(SignupRequestStatus.PENDING)
                .build();

        entity = signupRepository.save(entity);

        boolean shouldAutoProvision = plan.isFree() && plan.isAutoApproveSignups();
        if (shouldAutoProvision) {
            return completeProvisioning(entity, plan, "system-auto-approve");
        }

        String message = plan.isFree()
                ? "Votre demande est en liste d'attente. Un administrateur validera votre espace sous 24–48 h."
                : "Votre demande a été enregistrée. Notre équipe vous recontacte pour finaliser l'offre.";

        return SignupSubmitResult.builder()
                .requestId(entity.getId())
                .status(SignupRequestStatus.PENDING)
                .provisioned(false)
                .message(message)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<SignupRequestResponse> listRequests(SignupRequestStatus status) {
        List<TenantSignupRequest> list = status == null
                ? signupRepository.findAllByOrderByCreatedAtDesc()
                : signupRepository.findByStatusOrderByCreatedAtDesc(status);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional
    public SignupSubmitResult approve(String requestId, String reviewedBy) {
        TenantSignupRequest entity = signupRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Demande introuvable"));

        if (entity.getStatus() == SignupRequestStatus.COMPLETED) {
            return SignupSubmitResult.builder()
                    .requestId(entity.getId())
                    .status(SignupRequestStatus.COMPLETED)
                    .provisioned(true)
                    .tenantId(entity.getTenantId())
                    .message("Demande déjà approuvée")
                    .createdAt(entity.getCreatedAt())
                    .build();
        }
        if (entity.getStatus() == SignupRequestStatus.REJECTED) {
            throw new IllegalArgumentException("Impossible d'approuver une demande rejetée");
        }

        SubscriptionPlan plan = planRepository.findById(entity.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan introuvable"));

        return completeProvisioning(entity, plan, reviewedBy != null ? reviewedBy : "superadmin");
    }

    @Transactional
    public SignupRequestResponse reject(String requestId, String reason, String reviewedBy) {
        TenantSignupRequest entity = signupRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Demande introuvable"));

        if (entity.getStatus() != SignupRequestStatus.PENDING) {
            throw new IllegalArgumentException("Seules les demandes en attente peuvent être rejetées");
        }

        entity.setStatus(SignupRequestStatus.REJECTED);
        entity.setRejectionReason(reason);
        entity.setReviewedBy(reviewedBy);
        entity.setReviewedAt(LocalDateTime.now());
        return toResponse(signupRepository.save(entity));
    }

    private SignupSubmitResult completeProvisioning(
            TenantSignupRequest entity,
            SubscriptionPlan plan,
            String reviewedBy
    ) {
        try {
            TenantProvisioningService.ProvisionResult result = provisioningService.provision(entity, plan);
            entity.setTenantId(result.getTenantId());
            entity.setStatus(SignupRequestStatus.COMPLETED);
            entity.setReviewedBy(reviewedBy);
            entity.setReviewedAt(LocalDateTime.now());
            entity.setProvisionError(null);
            signupRepository.save(entity);

            return SignupSubmitResult.builder()
                    .requestId(entity.getId())
                    .status(SignupRequestStatus.COMPLETED)
                    .provisioned(true)
                    .tenantId(result.getTenantId())
                    .temporaryPassword(result.getTemporaryPassword())
                    .message("Votre espace eHealth est prêt. Connectez-vous avec votre email administrateur.")
                    .createdAt(entity.getCreatedAt())
                    .build();
        } catch (RuntimeException e) {
            entity.setProvisionError(e.getMessage());
            signupRepository.save(entity);
            throw e;
        }
    }

    private SignupRequestResponse toResponse(TenantSignupRequest entity) {
        return SignupRequestResponse.builder()
                .id(entity.getId())
                .organizationName(entity.getOrganizationName())
                .subdomain(entity.getSubdomain())
                .adminEmail(entity.getAdminEmail())
                .adminFirstName(entity.getAdminFirstName())
                .adminLastName(entity.getAdminLastName())
                .adminPhone(entity.getAdminPhone())
                .planId(entity.getPlanId())
                .planName(entity.getPlan() != null ? entity.getPlan().getName() : null)
                .planFree(entity.getPlan() != null && entity.getPlan().isFree())
                .status(entity.getStatus())
                .tenantId(entity.getTenantId())
                .reviewedBy(entity.getReviewedBy())
                .reviewedAt(entity.getReviewedAt())
                .rejectionReason(entity.getRejectionReason())
                .provisionError(entity.getProvisionError())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
