package com.holytrinity.expenso.plan.adapter.out.persistence;

import com.holytrinity.expenso.plan.domain.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataGoalRepository extends JpaRepository<Goal, String> {
    List<Goal> findByUser_UserId(String userId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM goals WHERE goal_id IN (:ids) AND user_id = :userId", nativeQuery = true)
    List<Goal> findAllWithDeletedByIdsAndUserId(@org.springframework.data.repository.query.Param("ids") List<String> ids, @org.springframework.data.repository.query.Param("userId") String userId);
}
