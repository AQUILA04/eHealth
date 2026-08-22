package com.sih.ris.dto;

import com.sih.ris.entity.RadiologyStudy;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class RadiologyDtos {
    private RadiologyDtos() { }
    public record CreateStudyRequest(@NotNull Long clinicalEncounterId, @NotBlank String patientRef, @NotBlank String procedureName,
        String procedureCode, @NotNull RadiologyStudy.Modality modality, RadiologyStudy.Priority priority, String requestedBy) { }
    public record ScheduleStudyRequest(@NotNull LocalDateTime scheduledAt, @NotBlank String assignedTechnologist, String assignedRadiologist) { }
    public record PerformStudyRequest(String pacsStudyUid, @DecimalMin(value = "0.0", inclusive = true) Double radiationDoseMgy) { }
    public record ReportStudyRequest(@NotBlank String reportText, @NotBlank String assignedRadiologist) { }
    public record StudyResponse(Long id, Long clinicalEncounterId, String patientRef, String procedureName, String procedureCode,
        RadiologyStudy.Modality modality, RadiologyStudy.Priority priority, RadiologyStudy.Status status, String requestedBy,
        String assignedRadiologist, String assignedTechnologist, String pacsStudyUid, String reportText, Double radiationDoseMgy,
        LocalDateTime requestedAt, LocalDateTime scheduledAt, LocalDateTime performedAt, LocalDateTime reportedAt) { }
}
