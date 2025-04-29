package com.raghad.mapviewerbackend.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {
    List<SavingGoal> findByUserId(Long userId);
}
