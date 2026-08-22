package com.sih.lis.service;

import com.sih.lis.dto.LaboratoryDtos.*;
import com.sih.lis.entity.LaboratoryOrder;
import com.sih.lis.entity.LaboratoryResult;
import com.sih.lis.repository.LaboratoryOrderRepository;
import com.sih.lis.repository.LaboratoryResultRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LaboratoryWorkflowService {
    private final LaboratoryOrderRepository orderRepository;
    private final LaboratoryResultRepository resultRepository;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        var now = LocalDateTime.now();
        var order = LaboratoryOrder.builder().clinicalEncounterId(request.clinicalEncounterId()).patientRef(request.patientRef())
            .examName(request.examName()).examCode(request.examCode()).sampleType(request.sampleType())
            .priority(request.priority() == null ? LaboratoryOrder.Priority.ROUTINE : request.priority())
            .status(LaboratoryOrder.Status.ORDERED).orderedBy(request.orderedBy()).orderedAt(now)
            .barcode("LIS-" + now.getYear() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase()).build();
        return toResponse(orderRepository.save(order));
    }

    public List<OrderResponse> list(LaboratoryOrder.Status status, String patientRef) {
        List<LaboratoryOrder> orders = status != null ? orderRepository.findByStatusOrderByOrderedAtAsc(status)
            : patientRef != null && !patientRef.isBlank() ? orderRepository.findByPatientRefOrderByOrderedAtDesc(patientRef)
            : orderRepository.findAll();
        return orders.stream().map(this::toResponse).toList();
    }

    public OrderResponse get(Long id) { return toResponse(find(id)); }

    @Transactional
    public OrderResponse collect(Long id, CollectSpecimenRequest request) {
        var order = find(id);
        requireStatus(order, LaboratoryOrder.Status.ORDERED);
        order.setStatus(LaboratoryOrder.Status.COLLECTED); order.setCollectedBy(request.collectedBy());
        order.setCollectedAt(request.collectedAt() == null ? LocalDateTime.now() : request.collectedAt());
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse receive(Long id, ReceiveSpecimenRequest request) {
        var order = find(id);
        requireStatus(order, LaboratoryOrder.Status.COLLECTED);
        order.setStatus(LaboratoryOrder.Status.RECEIVED); order.setReceivedBy(request.receivedBy());
        order.setReceivedAt(request.receivedAt() == null ? LocalDateTime.now() : request.receivedAt());
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse addResult(Long id, CreateResultRequest request) {
        var order = find(id);
        if (order.getStatus() != LaboratoryOrder.Status.RECEIVED && order.getStatus() != LaboratoryOrder.Status.IN_ANALYSIS) {
            throw new IllegalStateException("Un résultat ne peut être saisi qu'après réception de l'échantillon.");
        }
        order.setStatus(LaboratoryOrder.Status.IN_ANALYSIS);
        var result = LaboratoryResult.builder().laboratoryOrder(order).analyteName(request.analyteName()).analyteCode(request.analyteCode())
            .resultValue(request.resultValue()).unit(request.unit()).referenceRange(request.referenceRange())
            .interpretation(request.interpretation()).technicalValidator(request.technicalValidator()).resultedAt(LocalDateTime.now()).build();
        resultRepository.save(result);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse validate(Long id, ValidateOrderRequest request) {
        var order = find(id);
        if (order.getResults().isEmpty()) throw new IllegalStateException("La validation exige au moins un résultat.");
        order.setStatus(LaboratoryOrder.Status.BIOLOGICALLY_VALIDATED); order.setValidatedBy(request.validatedBy()); order.setValidatedAt(LocalDateTime.now());
        log.info("LIS: ordre {} validé par {}", id, request.validatedBy());
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse notifyCritical(Long id, CriticalNotificationRequest request) {
        var order = find(id);
        boolean hasCritical = order.getResults().stream().anyMatch(r -> r.getInterpretation() == LaboratoryResult.Interpretation.CRITICAL_LOW || r.getInterpretation() == LaboratoryResult.Interpretation.CRITICAL_HIGH);
        if (!hasCritical) throw new IllegalStateException("Aucune valeur critique n'est présente pour cet ordre.");
        order.setCriticalNotifiedTo(request.notifiedTo()); order.setCriticalNotifiedAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    private LaboratoryOrder find(Long id) { return orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ordre de laboratoire introuvable: " + id)); }
    private void requireStatus(LaboratoryOrder order, LaboratoryOrder.Status status) { if (order.getStatus() != status) throw new IllegalStateException("Transition invalide depuis le statut " + order.getStatus()); }
    private OrderResponse toResponse(LaboratoryOrder order) {
        var results = order.getResults().stream().map(r -> new ResultResponse(r.getId(), r.getAnalyteName(), r.getAnalyteCode(), r.getResultValue(), r.getUnit(), r.getReferenceRange(), r.getInterpretation(), r.getTechnicalValidator(), r.getResultedAt())).toList();
        return new OrderResponse(order.getId(), order.getClinicalEncounterId(), order.getPatientRef(), order.getExamName(), order.getExamCode(), order.getSampleType(), order.getBarcode(), order.getPriority(), order.getStatus(), order.getOrderedBy(), order.getCollectedBy(), order.getReceivedBy(), order.getValidatedBy(), order.getOrderedAt(), order.getCollectedAt(), order.getReceivedAt(), order.getValidatedAt(), order.getCriticalNotifiedAt(), order.getCriticalNotifiedTo(), results);
    }
}
