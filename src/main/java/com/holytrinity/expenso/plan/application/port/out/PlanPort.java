package com.holytrinity.expenso.plan.application.port.out;

import com.holytrinity.expenso.plan.domain.Goal;
import com.holytrinity.expenso.plan.domain.Subscription;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PlanPort {
    Page<Goal> findAllGoals(Pageable pageable);
    Optional<Goal> loadGoal(String goalId);
    void deleteGoal(Goal goal);

    Page<Subscription> findAllSubscriptions(Pageable pageable);
    Optional<Subscription> loadSubscription(String subscriptionId);
    void deleteSubscription(Subscription subscription);

    List<Goal> saveAllGoals(List<Goal> goals);
    List<Subscription> saveAllSubscriptions(List<Subscription> subscriptions);
    List<Goal> findGoalsByUserId(String userId);
    List<Subscription> findSubscriptionsByUserId(String userId);
    List<Goal> findGoalsWithDeletedByIdsAndUserId(List<String> ids, String userId);
    List<Subscription> findSubscriptionsWithDeletedByIdsAndUserId(List<String> ids, String userId);
}
