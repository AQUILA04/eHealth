package com.sih.lis.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "laboratory_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LaboratoryResult {
    public enum Interpretation { NORMAL, LOW, HIGH, ABNORMAL, CRITICAL_LOW, CRITICAL_HIGH }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "laboratory_order_id") private LaboratoryOrder laboratoryOrder;
    @Column(nullable = false) private String analyteName;
    private String analyteCode;
    @Column(nullable = false) private String resultValue;
    private String unit;
    private String referenceRange;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Interpretation interpretation;
    private String technicalValidator;
    private LocalDateTime resultedAt;
}
