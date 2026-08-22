package com.sih.pharmacy.service;

import com.sih.pharmacy.dto.PharmacyDtos.*;
import com.sih.pharmacy.entity.*;
import com.sih.pharmacy.repository.*;
import com.sih.shared.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional(readOnly = true)
public class PharmacyWorkflowService {
    private final MedicationProductRepository productRepository;
    private final InventoryLotRepository lotRepository;
    private final DispensationRepository dispensationRepository;
    @Transactional public ProductResponse createProduct(CreateProductRequest request) {
        productRepository.findByTenantIdAndSku(currentTenant(), request.sku()).ifPresent(p -> { throw new IllegalArgumentException("SKU déjà utilisé: " + request.sku()); });
        var product = MedicationProduct.builder().sku(request.sku()).name(request.name()).genericName(request.genericName()).atcCode(request.atcCode()).unit(request.unit()).minimumStock(request.minimumStock()).active(true).build();
        return toProductResponse(productRepository.save(product));
    }
    public List<ProductResponse> listProducts() { return productRepository.findByTenantIdAndActiveTrueOrderByNameAsc(currentTenant()).stream().map(this::toProductResponse).toList(); }
    @Transactional public LotResponse receiveLot(ReceiveLotRequest request) {
        var product = findProduct(request.productId());
        var lot = InventoryLot.builder().product(product).lotNumber(request.lotNumber()).quantityOnHand(request.quantity()).expiryDate(request.expiryDate()).storageLocation(request.storageLocation()).supplier(request.supplier()).build();
        return toLotResponse(lotRepository.save(lot));
    }
    public List<LotResponse> listLots(Long productId) { String tenantId = currentTenant(); var lots = productId == null ? lotRepository.findByTenantIdOrderByExpiryDateAsc(tenantId) : lotRepository.findByTenantIdAndProductIdOrderByExpiryDateAsc(tenantId, productId); return lots.stream().map(this::toLotResponse).toList(); }
    @Transactional public DispensationResponse validate(CreateDispensationRequest request) {
        var product = findProduct(request.productId());
        var available = lotRepository.findByTenantIdAndProductIdAndExpiryDateGreaterThanOrderByExpiryDateAsc(currentTenant(), product.getId(), LocalDate.now()).stream().mapToInt(InventoryLot::getQuantityOnHand).sum();
        if (available < request.quantity()) throw new IllegalStateException("Stock disponible insuffisant pour " + product.getName());
        var dispensation = Dispensation.builder().clinicalEncounterId(request.clinicalEncounterId()).patientRef(request.patientRef()).product(product).quantity(request.quantity()).status(Dispensation.Status.VALIDATED).pharmacist(request.pharmacist()).clinicalPrescriptionRef(request.clinicalPrescriptionRef()).validatedAt(LocalDateTime.now()).build();
        return toDispensationResponse(dispensationRepository.save(dispensation));
    }
    @Transactional public DispensationResponse dispense(Long id, DispenseRequest request) {
        var dispensation = dispensationRepository.findByIdAndTenantId(id, currentTenant()).orElseThrow(() -> new EntityNotFoundException("Dispensation introuvable: " + id));
        if (dispensation.getStatus() != Dispensation.Status.VALIDATED) throw new IllegalStateException("Seule une dispensation validée peut être délivrée.");
        InventoryLot lot = request.lotId() == null ? lotRepository.findByTenantIdAndProductIdAndExpiryDateGreaterThanOrderByExpiryDateAsc(currentTenant(), dispensation.getProduct().getId(), LocalDate.now()).stream().filter(l -> l.getQuantityOnHand() >= dispensation.getQuantity()).findFirst().orElseThrow(() -> new IllegalStateException("Aucun lot FEFO ne couvre la quantité demandée.")) : lotRepository.findByIdAndTenantId(request.lotId(), currentTenant()).orElseThrow(() -> new EntityNotFoundException("Lot introuvable: " + request.lotId()));
        if (!lot.getProduct().getId().equals(dispensation.getProduct().getId()) || lot.getExpiryDate().isBefore(LocalDate.now()) || lot.getQuantityOnHand() < dispensation.getQuantity()) throw new IllegalStateException("Lot non éligible à la dispensation.");
        lot.setQuantityOnHand(lot.getQuantityOnHand() - dispensation.getQuantity());
        dispensation.setLot(lot); dispensation.setStatus(Dispensation.Status.DISPENSED); dispensation.setDispensedAt(LocalDateTime.now());
        lotRepository.save(lot); log.info("Pharmacie: dispensation {} délivrée depuis lot {}", id, lot.getLotNumber());
        return toDispensationResponse(dispensationRepository.save(dispensation));
    }
    public List<DispensationResponse> listDispensations(String patientRef) { String tenantId = currentTenant(); var rows = patientRef == null || patientRef.isBlank() ? dispensationRepository.findByTenantIdOrderByValidatedAtDesc(tenantId) : dispensationRepository.findByTenantIdAndPatientRefOrderByValidatedAtDesc(tenantId, patientRef); return rows.stream().map(this::toDispensationResponse).toList(); }
    private MedicationProduct findProduct(Long id) { return productRepository.findByIdAndTenantId(id, currentTenant()).orElseThrow(() -> new EntityNotFoundException("Produit introuvable: " + id)); }
    private String currentTenant() { return TenantContext.requireCurrentTenant(); }
    private ProductResponse toProductResponse(MedicationProduct p) { int quantity = lotRepository.findByTenantIdAndProductIdOrderByExpiryDateAsc(currentTenant(), p.getId()).stream().filter(l -> !l.getExpiryDate().isBefore(LocalDate.now())).mapToInt(InventoryLot::getQuantityOnHand).sum(); return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getGenericName(), p.getAtcCode(), p.getUnit(), p.getMinimumStock(), quantity, quantity <= p.getMinimumStock(), p.isActive()); }
    private LotResponse toLotResponse(InventoryLot l) { return new LotResponse(l.getId(), l.getProduct().getId(), l.getProduct().getName(), l.getLotNumber(), l.getQuantityOnHand(), l.getExpiryDate(), l.getStorageLocation(), l.getSupplier(), !l.getExpiryDate().isAfter(LocalDate.now().plusDays(90))); }
    private DispensationResponse toDispensationResponse(Dispensation d) { return new DispensationResponse(d.getId(), d.getClinicalEncounterId(), d.getPatientRef(), d.getProduct().getId(), d.getProduct().getName(), d.getLot() == null ? null : d.getLot().getId(), d.getLot() == null ? null : d.getLot().getLotNumber(), d.getQuantity(), d.getStatus(), d.getPharmacist(), d.getClinicalPrescriptionRef(), d.getValidatedAt(), d.getDispensedAt()); }
}
