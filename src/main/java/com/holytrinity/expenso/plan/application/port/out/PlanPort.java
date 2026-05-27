package com.holytrinity.expenso.plan.application.port.out;

import com.holytrinity.expenso.plan.domain.Goal;
import com.holytrinity.expenso.plan.domain.Subscription;
import java.util.List;

public interface PlanPort {
    List<Goal> saveAllGoals(List<Goal> goals);
    List<Subscription> saveAllSubscriptions(List<Subscription> subscriptions);
    List<Goal> findGoalsByUserId(String userId);
    List<Subscription> findSubscriptionsByUserId(String userId);
}
