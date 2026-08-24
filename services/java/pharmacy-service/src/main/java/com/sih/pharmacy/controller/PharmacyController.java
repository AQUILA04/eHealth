package com.sih.pharmacy.controller;

import com.sih.pharmacy.dto.PharmacyDtos.*;
import com.sih.pharmacy.service.PharmacyWorkflowService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/pharmacy") @RequiredArgsConstructor
public class PharmacyController {
    private final PharmacyWorkflowService service;
    @PostMapping("/products") public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) { var response = service.createProduct(request); return ResponseEntity.created(URI.create("/api/v1/pharmacy/products/" + response.id())).body(response); }
    @GetMapping("/products") public List<ProductResponse> listProducts() { return service.listProducts(); }
    @PostMapping("/inventory/receipts") public ResponseEntity<LotResponse> receiveLot(@Valid @RequestBody ReceiveLotRequest request) { var response = service.receiveLot(request); return ResponseEntity.created(URI.create("/api/v1/pharmacy/inventory/lots/" + response.id())).body(response); }
    @GetMapping("/inventory/lots") public List<LotResponse> listLots(@RequestParam(required = false) Long productId) { return service.listLots(productId); }
    @PostMapping("/dispensations") public ResponseEntity<DispensationResponse> validate(@Valid @RequestBody CreateDispensationRequest request) { var response = service.validate(request); return ResponseEntity.created(URI.create("/api/v1/pharmacy/dispensations/" + response.id())).body(response); }
    @GetMapping("/dispensations") public List<DispensationResponse> listDispensations(@RequestParam(required = false) String patientRef) { return service.listDispensations(patientRef); }
    @PatchMapping("/dispensations/{id}/dispense") public DispensationResponse dispense(@PathVariable Long id, @RequestBody(required = false) DispenseRequest request) { return service.dispense(id, request == null ? new DispenseRequest(null) : request); }
}
