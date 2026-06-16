package com.holytrinity.expenso.plan.application.port.in;

import com.holytrinity.expenso.plan.application.dto.GoalDTO;
import com.holytrinity.expenso.plan.application.dto.SubscriptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PlanUseCase {
    Page<GoalDTO> findAllGoals(Pageable pageable);
    GoalDTO getGoal(String goalId);
    GoalDTO updateGoal(String goalId, GoalDTO dto);
    void deleteGoal(String goalId);
    void deleteBulkGoals(List<String> goalIds);
    List<GoalDTO> processBulkGoals(String userId, List<GoalDTO> incomingGoals);

    Page<SubscriptionDTO> findAllSubscriptions(Pageable pageable);
    SubscriptionDTO getSubscription(String subscriptionId);
    SubscriptionDTO updateSubscription(String subscriptionId, SubscriptionDTO dto);
    void deleteSubscription(String subscriptionId);
    void deleteBulkSubscriptions(List<String> subscriptionIds);
    List<SubscriptionDTO> processBulkSubscriptions(String userId, List<SubscriptionDTO> incomingSubscriptions);
}
