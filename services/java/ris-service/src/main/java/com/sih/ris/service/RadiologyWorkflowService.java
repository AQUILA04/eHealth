package com.sih.ris.service;

import com.sih.ris.dto.RadiologyDtos.*;
import com.sih.ris.entity.RadiologyStudy;
import com.sih.ris.repository.RadiologyStudyRepository;
import com.sih.shared.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional(readOnly = true)
public class RadiologyWorkflowService {
    private final RadiologyStudyRepository repository;
    @Transactional public StudyResponse create(CreateStudyRequest request) {
        var study = RadiologyStudy.builder().clinicalEncounterId(request.clinicalEncounterId()).patientRef(request.patientRef()).procedureName(request.procedureName()).procedureCode(request.procedureCode()).modality(request.modality()).priority(request.priority() == null ? RadiologyStudy.Priority.ROUTINE : request.priority()).status(RadiologyStudy.Status.REQUESTED).requestedBy(request.requestedBy()).requestedAt(LocalDateTime.now()).build();
        return toResponse(repository.save(study));
    }
    public List<StudyResponse> list(RadiologyStudy.Status status, String patientRef) {
        String tenantId = currentTenant();
        var studies = status != null ? repository.findByTenantIdAndStatusOrderByScheduledAtAsc(tenantId, status) : patientRef != null && !patientRef.isBlank() ? repository.findByTenantIdAndPatientRefOrderByRequestedAtDesc(tenantId, patientRef) : repository.findByTenantIdOrderByRequestedAtAsc(tenantId);
        return studies.stream().map(this::toResponse).toList();
    }
    public StudyResponse get(Long id) { return toResponse(find(id)); }
    @Transactional public StudyResponse schedule(Long id, ScheduleStudyRequest request) {
        var study = find(id); requireStatus(study, RadiologyStudy.Status.REQUESTED); study.setStatus(RadiologyStudy.Status.SCHEDULED); study.setScheduledAt(request.scheduledAt()); study.setAssignedTechnologist(request.assignedTechnologist()); study.setAssignedRadiologist(request.assignedRadiologist()); return toResponse(repository.save(study));
    }
    @Transactional public StudyResponse checkIn(Long id) { var study = find(id); requireStatus(study, RadiologyStudy.Status.SCHEDULED); study.setStatus(RadiologyStudy.Status.CHECKED_IN); return toResponse(repository.save(study)); }
    @Transactional public StudyResponse perform(Long id, PerformStudyRequest request) {
        var study = find(id); if (study.getStatus() != RadiologyStudy.Status.SCHEDULED && study.getStatus() != RadiologyStudy.Status.CHECKED_IN) throw new IllegalStateException("L'examen doit être planifié ou accueilli avant réalisation."); study.setStatus(RadiologyStudy.Status.COMPLETED); study.setPacsStudyUid(request.pacsStudyUid()); study.setRadiationDoseMgy(request.radiationDoseMgy()); study.setPerformedAt(LocalDateTime.now()); return toResponse(repository.save(study));
    }
    @Transactional public StudyResponse report(Long id, ReportStudyRequest request) {
        var study = find(id); requireStatus(study, RadiologyStudy.Status.COMPLETED); study.setStatus(RadiologyStudy.Status.REPORTED); study.setReportText(request.reportText()); study.setAssignedRadiologist(request.assignedRadiologist()); study.setReportedAt(LocalDateTime.now()); log.info("RIS: compte-rendu validé pour l'étude {}", id); return toResponse(repository.save(study));
    }
    private RadiologyStudy find(Long id) { return repository.findByIdAndTenantId(id, currentTenant()).orElseThrow(() -> new EntityNotFoundException("Étude radiologique introuvable: " + id)); }
    private String currentTenant() { return TenantContext.requireCurrentTenant(); }
    private void requireStatus(RadiologyStudy study, RadiologyStudy.Status status) { if (study.getStatus() != status) throw new IllegalStateException("Transition invalide depuis le statut " + study.getStatus()); }
    private StudyResponse toResponse(RadiologyStudy s) { return new StudyResponse(s.getId(), s.getClinicalEncounterId(), s.getPatientRef(), s.getProcedureName(), s.getProcedureCode(), s.getModality(), s.getPriority(), s.getStatus(), s.getRequestedBy(), s.getAssignedRadiologist(), s.getAssignedTechnologist(), s.getPacsStudyUid(), s.getReportText(), s.getRadiationDoseMgy(), s.getRequestedAt(), s.getScheduledAt(), s.getPerformedAt(), s.getReportedAt()); }
}
