package com.sih.pharmacy.repository;
import com.sih.pharmacy.entity.InventoryLot;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InventoryLotRepository extends JpaRepository<InventoryLot, Long> { List<InventoryLot> findByProductIdOrderByExpiryDateAsc(Long productId); List<InventoryLot> findByProductIdAndExpiryDateGreaterThanOrderByExpiryDateAsc(Long productId, LocalDate date); }
