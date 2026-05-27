package com.holytrinity.expenso.plan.application.port.in;

import com.holytrinity.expenso.plan.application.dto.GoalDTO;
import com.holytrinity.expenso.plan.application.dto.SubscriptionDTO;
import java.util.List;

public interface PlanUseCase {
    List<GoalDTO> processBulkGoals(String userId, List<GoalDTO> incomingGoals);
    List<SubscriptionDTO> processBulkSubscriptions(String userId, List<SubscriptionDTO> incomingSubscriptions);
}
