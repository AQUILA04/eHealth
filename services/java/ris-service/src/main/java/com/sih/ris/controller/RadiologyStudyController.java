package com.sih.ris.controller;

import com.sih.ris.dto.RadiologyDtos.*;
import com.sih.ris.entity.RadiologyStudy;
import com.sih.ris.service.RadiologyWorkflowService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/ris/studies") @RequiredArgsConstructor
public class RadiologyStudyController {
    private final RadiologyWorkflowService service;
    @PostMapping public ResponseEntity<StudyResponse> create(@Valid @RequestBody CreateStudyRequest request) { var response = service.create(request); return ResponseEntity.created(URI.create("/api/v1/ris/studies/" + response.id())).body(response); }
    @GetMapping public List<StudyResponse> list(@RequestParam(required = false) RadiologyStudy.Status status, @RequestParam(required = false) String patientRef) { return service.list(status, patientRef); }
    @GetMapping("/{id}") public StudyResponse get(@PathVariable Long id) { return service.get(id); }
    @PatchMapping("/{id}/schedule") public StudyResponse schedule(@PathVariable Long id, @Valid @RequestBody ScheduleStudyRequest request) { return service.schedule(id, request); }
    @PatchMapping("/{id}/check-in") public StudyResponse checkIn(@PathVariable Long id) { return service.checkIn(id); }
    @PatchMapping("/{id}/perform") public StudyResponse perform(@PathVariable Long id, @Valid @RequestBody PerformStudyRequest request) { return service.perform(id, request); }
    @PatchMapping("/{id}/report") public StudyResponse report(@PathVariable Long id, @Valid @RequestBody ReportStudyRequest request) { return service.report(id, request); }
}
