package com.sih.rcm.repository;
import com.sih.rcm.entity.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PaymentRepository extends JpaRepository<Payment, Long> { List<Payment> findByInvoiceIdAndTenantIdOrderByReceivedAtDesc(Long invoiceId, String tenantId); }
