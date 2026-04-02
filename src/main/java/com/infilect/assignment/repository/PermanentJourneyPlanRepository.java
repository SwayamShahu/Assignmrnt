package com.infilect.assignment.repository;

import com.infilect.assignment.entity.PermanentJourneyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface PermanentJourneyPlanRepository extends JpaRepository<PermanentJourneyPlan, Long> {
    boolean existsByUserIdAndStoreIdAndDate(Long userId, Long storeId, LocalDate date);
}
