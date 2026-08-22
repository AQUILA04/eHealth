package com.sih.pharmacy.repository;
import com.sih.pharmacy.entity.MedicationProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MedicationProductRepository extends JpaRepository<MedicationProduct, Long> { Optional<MedicationProduct> findBySku(String sku); List<MedicationProduct> findByActiveTrueOrderByNameAsc(); }
