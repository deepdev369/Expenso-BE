package com.holytrinity.expenso.plan.adapter.out.persistence;

import com.holytrinity.expenso.plan.domain.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataGoalRepository extends JpaRepository<Goal, String> {
    List<Goal> findByUser_UserId(String userId);
}
