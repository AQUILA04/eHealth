package com.sih.tenant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_signup_requests", indexes = {
        @Index(name = "idx_signup_status", columnList = "status"),
        @Index(name = "idx_signup_admin_email", columnList = "adminEmail")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantSignupRequest {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 150)
    private String organizationName;

    @Column(length = 100)
    private String subdomain;

    @Column(nullable = false, length = 150)
    private String adminEmail;

    @Column(nullable = false, length = 80)
    private String adminFirstName;

    @Column(nullable = false, length = 80)
    private String adminLastName;

    @Column(length = 50)
    private String adminPhone;

    @Column(nullable = false, length = 36)
    private String planId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "planId", insertable = false, updatable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private SignupRequestStatus status = SignupRequestStatus.PENDING;

    @Column(length = 50)
    private String tenantId;

    @Column(length = 150)
    private String reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 500)
    private String provisionError;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void ensureId() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
