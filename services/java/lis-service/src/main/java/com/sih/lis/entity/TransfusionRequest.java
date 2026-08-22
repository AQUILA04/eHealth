package com.sih.lis.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "transfusion_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransfusionRequest {
    public enum Status { REQUESTED, COMPATIBILITY_VALIDATED, ISSUED, COMPLETED, REACTION_REPORTED, CANCELLED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long clinicalEncounterId;
    @Column(nullable = false) private String patientRef;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BloodUnit.AboGroup recipientAboGroup;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BloodUnit.Rhesus recipientRhesus;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BloodUnit.Component component;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private BloodUnit bloodUnit;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    private String requestedBy;
    private String crossmatchValidatedBy;
    private String issuedBy;
    private String completedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime crossmatchValidatedAt;
    private LocalDateTime issuedAt;
    private LocalDateTime completedAt;
    @Column(length = 2000) private String reactionDescription;
    private LocalDateTime reactionReportedAt;
}
