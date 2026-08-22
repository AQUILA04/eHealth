package com.sih.lis.controller;

import com.sih.lis.dto.BloodBankDtos.*;
import com.sih.lis.entity.BloodUnit;
import com.sih.lis.entity.TransfusionRequest;
import com.sih.lis.service.BloodBankService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/lis/blood-bank") @RequiredArgsConstructor
public class BloodBankController {
    private final BloodBankService service;
    @PostMapping("/units") public ResponseEntity<BloodUnitResponse> receive(@Valid @RequestBody ReceiveBloodUnitRequest request) { var response = service.receive(request); return ResponseEntity.created(URI.create("/api/v1/lis/blood-bank/units/" + response.id())).body(response); }
    @GetMapping("/units") public List<BloodUnitResponse> listUnits(@RequestParam(required = false) BloodUnit.Status status) { return service.listUnits(status); }
    @PostMapping("/transfusions") public ResponseEntity<TransfusionResponse> request(@Valid @RequestBody CreateTransfusionRequest request) { var response = service.request(request); return ResponseEntity.created(URI.create("/api/v1/lis/blood-bank/transfusions/" + response.id())).body(response); }
    @GetMapping("/transfusions") public List<TransfusionResponse> listTransfusions(@RequestParam(required = false) TransfusionRequest.Status status, @RequestParam(required = false) String patientRef) { return service.listTransfusions(status, patientRef); }
    @PatchMapping("/transfusions/{id}/crossmatch") public TransfusionResponse crossmatch(@PathVariable Long id, @Valid @RequestBody CrossmatchRequest request) { return service.validateCrossmatch(id, request); }
    @PatchMapping("/transfusions/{id}/issue") public TransfusionResponse issue(@PathVariable Long id, @Valid @RequestBody IssueRequest request) { return service.issue(id, request); }
    @PatchMapping("/transfusions/{id}/complete") public TransfusionResponse complete(@PathVariable Long id, @Valid @RequestBody CompleteTransfusionRequest request) { return service.complete(id, request); }
    @PostMapping("/transfusions/{id}/reaction") public TransfusionResponse reaction(@PathVariable Long id, @Valid @RequestBody ReportReactionRequest request) { return service.reportReaction(id, request); }
}
