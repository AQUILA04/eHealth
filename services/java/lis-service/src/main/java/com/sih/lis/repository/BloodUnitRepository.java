package com.sih.lis.repository;
import com.sih.lis.entity.BloodUnit;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BloodUnitRepository extends JpaRepository<BloodUnit, Long> {
    List<BloodUnit> findByStatusAndComponentAndExpiresOnGreaterThanEqualOrderByExpiresOnAsc(BloodUnit.Status status, BloodUnit.Component component, LocalDate expiresOn);
    List<BloodUnit> findByStatusOrderByExpiresOnAsc(BloodUnit.Status status);
}
