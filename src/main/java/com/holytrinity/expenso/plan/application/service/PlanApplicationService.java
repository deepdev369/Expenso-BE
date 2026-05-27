package com.holytrinity.expenso.plan.application.service;

import com.holytrinity.expenso.plan.application.dto.GoalDTO;
import com.holytrinity.expenso.plan.application.dto.SubscriptionDTO;
import com.holytrinity.expenso.plan.application.port.in.PlanUseCase;
import com.holytrinity.expenso.plan.application.port.out.PlanPort;
import com.holytrinity.expenso.plan.domain.Goal;
import com.holytrinity.expenso.plan.domain.Subscription;
import com.holytrinity.expenso.user.application.port.out.UserPort;
import com.holytrinity.expenso.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanApplicationService implements PlanUseCase {

    private final PlanPort planPort;
    private final UserPort userPort;

    public PlanApplicationService(PlanPort planPort, UserPort userPort) {
        this.planPort = planPort;
        this.userPort = userPort;
    }

    @Override
    @Transactional
    public List<GoalDTO> processBulkGoals(String userId, List<GoalDTO> incomingGoals) {
        User user = userPort.loadUser(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<Goal> existingGoals = planPort.findGoalsByUserId(userId);
        Map<String, Goal> existingGoalMap = existingGoals.stream()
                .collect(Collectors.toMap(Goal::getGoalId, g -> g));

        List<Goal> goalsToSave = new ArrayList<>();

        for (GoalDTO dto : incomingGoals) {
            Goal goal = existingGoalMap.get(dto.getGoalId());
            if (goal == null) {
                goal = new Goal();
                goal.setGoalId(dto.getGoalId());
                goal.setUser(user);
            }

            goal.setDeleted(dto.getDeleted() != null ? dto.getDeleted() : false);
            goal.setName(dto.getName());
            goal.setCurrentAmount(dto.getCurrentAmount());
            goal.setTargetAmount(dto.getTargetAmount());
            goal.setProjectedDate(dto.getProjectedDate());
            goal.setColorHex(dto.getColorHex());
            goal.setIsCompleted(dto.getIsCompleted() != null ? dto.getIsCompleted() : false);
            goal.setAiSuggestion(dto.getAiSuggestion());
            goal.setGoalDateCreated(dto.getDateCreated());
            goal.setGoalLastUpdated(dto.getLastUpdated());

            goalsToSave.add(goal);
        }

        List<Goal> savedGoals = planPort.saveAllGoals(goalsToSave);

        return savedGoals.stream().map(g -> GoalDTO.builder()
                .goalId(g.getGoalId())
                .version(g.getVersion())
                .deleted(g.getDeleted())
                .name(g.getName())
                .currentAmount(g.getCurrentAmount())
                .targetAmount(g.getTargetAmount())
                .projectedDate(g.getProjectedDate())
                .colorHex(g.getColorHex())
                .isCompleted(g.getIsCompleted())
                .aiSuggestion(g.getAiSuggestion())
                .userId(g.getUser().getUserId())
                .dateCreated(g.getGoalDateCreated())
                .lastUpdated(g.getGoalLastUpdated())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SubscriptionDTO> processBulkSubscriptions(String userId, List<SubscriptionDTO> incomingSubscriptions) {
        User user = userPort.loadUser(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<Subscription> existingSubscriptions = planPort.findSubscriptionsByUserId(userId);
        Map<String, Subscription> existingSubMap = existingSubscriptions.stream()
                .collect(Collectors.toMap(Subscription::getSubscriptionId, s -> s));

        List<Subscription> subscriptionsToSave = new ArrayList<>();

        for (SubscriptionDTO dto : incomingSubscriptions) {
            Subscription subscription = existingSubMap.get(dto.getSubscriptionId());
            if (subscription == null) {
                subscription = new Subscription();
                subscription.setSubscriptionId(dto.getSubscriptionId());
                subscription.setUser(user);
            }

            subscription.setDeleted(dto.getDeleted() != null ? dto.getDeleted() : false);
            subscription.setName(dto.getName());
            subscription.setMerchant(dto.getMerchant());
            subscription.setAmount(dto.getAmount());
            subscription.setRenewalDate(dto.getRenewalDate());
            subscription.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
            subscription.setColorHex(dto.getColorHex());
            subscription.setSubscriptionDateCreated(dto.getDateCreated());
            subscription.setSubscriptionLastUpdated(dto.getLastUpdated());

            subscriptionsToSave.add(subscription);
        }

        List<Subscription> savedSubs = planPort.saveAllSubscriptions(subscriptionsToSave);

        return savedSubs.stream().map(s -> SubscriptionDTO.builder()
                .subscriptionId(s.getSubscriptionId())
                .version(s.getVersion())
                .deleted(s.getDeleted())
                .name(s.getName())
                .merchant(s.getMerchant())
                .amount(s.getAmount())
                .renewalDate(s.getRenewalDate())
                .isActive(s.getIsActive())
                .colorHex(s.getColorHex())
                .userId(s.getUser().getUserId())
                .dateCreated(s.getSubscriptionDateCreated())
                .lastUpdated(s.getSubscriptionLastUpdated())
                .build()).collect(Collectors.toList());
    }
}
