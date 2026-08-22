package com.sih.rcm.repository;
import com.sih.rcm.entity.Invoice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InvoiceRepository extends JpaRepository<Invoice, Long> { List<Invoice> findByTenantIdOrderByCreatedAtDesc(String tenantId); Optional<Invoice> findByIdAndTenantId(Long id, String tenantId); }
