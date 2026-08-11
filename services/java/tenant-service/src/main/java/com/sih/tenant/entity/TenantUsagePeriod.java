package com.sih.tenant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_usage_periods")
@IdClass(TenantUsagePeriod.Pk.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantUsagePeriod {

    @Id
    @Column(length = 50)
    private String tenantId;

    @Id
    @Column(length = 80)
    private String operationKey;

    @Id
    @Column(length = 20)
    private String periodType;

    @Id
    @Column(length = 40)
    private String periodKey;

    @Column(nullable = false)
    @Builder.Default
    private long countValue = 0;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private String tenantId;
        private String operationKey;
        private String periodType;
        private String periodKey;
    }
}
