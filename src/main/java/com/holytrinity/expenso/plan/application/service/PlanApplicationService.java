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

    private final com.holytrinity.expenso.security.UserContext userContext;

    public PlanApplicationService(PlanPort planPort, UserPort userPort, com.holytrinity.expenso.security.UserContext userContext) {
        this.planPort = planPort;
        this.userPort = userPort;
        this.userContext = userContext;
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<GoalDTO> findAllGoals(org.springframework.data.domain.Pageable pageable) {
        return planPort.findAllGoals(pageable).map(this::mapGoalToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalDTO getGoal(String goalId) {
        Goal goal = planPort.loadGoal(goalId)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkGoalOwnership(goal);
        return mapGoalToDTO(goal);
    }

    @Override
    @Transactional
    public GoalDTO updateGoal(String goalId, GoalDTO dto) {
        Goal goal = planPort.loadGoal(goalId)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkGoalOwnership(goal);
        
        goal.setName(dto.getName());
        goal.setCurrentAmount(dto.getCurrentAmount());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setProjectedDate(dto.getProjectedDate());
        goal.setColorHex(dto.getColorHex());
        goal.setIsCompleted(dto.getIsCompleted() != null ? dto.getIsCompleted() : false);
        goal.setAiSuggestion(dto.getAiSuggestion());

        Goal updatedGoal = planPort.saveAllGoals(List.of(goal)).get(0);
        return mapGoalToDTO(updatedGoal);
    }

    @Override
    @Transactional
    public void deleteGoal(String goalId) {
        Goal goal = planPort.loadGoal(goalId)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkGoalOwnership(goal);
        goal.setDeleted(true);
        planPort.saveAllGoals(List.of(goal));
    }

    @Override
    @Transactional
    public void deleteBulkGoals(List<String> goalIds) {
        goalIds.forEach(id -> {
            planPort.loadGoal(id).ifPresent(goal -> {
                deleteGoal(goal.getGoalId());
            });
        });
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SubscriptionDTO> findAllSubscriptions(org.springframework.data.domain.Pageable pageable) {
        return planPort.findAllSubscriptions(pageable).map(this::mapSubscriptionToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDTO getSubscription(String subscriptionId) {
        Subscription subscription = planPort.loadSubscription(subscriptionId)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkSubscriptionOwnership(subscription);
        return mapSubscriptionToDTO(subscription);
    }

    @Override
    @Transactional
    public SubscriptionDTO updateSubscription(String subscriptionId, SubscriptionDTO dto) {
        Subscription subscription = planPort.loadSubscription(subscriptionId)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkSubscriptionOwnership(subscription);

        subscription.setName(dto.getName());
        subscription.setMerchant(dto.getMerchant());
        subscription.setAmount(dto.getAmount());
        subscription.setRenewalDate(dto.getRenewalDate());
        subscription.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        subscription.setColorHex(dto.getColorHex());

        Subscription updatedSub = planPort.saveAllSubscriptions(List.of(subscription)).get(0);
        return mapSubscriptionToDTO(updatedSub);
    }

    @Override
    @Transactional
    public void deleteSubscription(String subscriptionId) {
        Subscription subscription = planPort.loadSubscription(subscriptionId)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkSubscriptionOwnership(subscription);
        subscription.setDeleted(true);
        planPort.saveAllSubscriptions(List.of(subscription));
    }

    @Override
    @Transactional
    public void deleteBulkSubscriptions(List<String> subscriptionIds) {
        subscriptionIds.forEach(id -> {
            planPort.loadSubscription(id).ifPresent(sub -> {
                deleteSubscription(sub.getSubscriptionId());
            });
        });
    }

    private GoalDTO mapGoalToDTO(Goal g) {
        return GoalDTO.builder()
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
                .build();
    }

    private SubscriptionDTO mapSubscriptionToDTO(Subscription s) {
        return SubscriptionDTO.builder()
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
                .build();
    }

    private void checkGoalOwnership(Goal goal) {
        String currentUserId = userContext.getCurrentUserId();
        if (!goal.getUser().getUserId().equals(currentUserId)) {
            throw new com.holytrinity.expenso.shared.exception.NotFoundException();
        }
    }

    private void checkSubscriptionOwnership(Subscription sub) {
        String currentUserId = userContext.getCurrentUserId();
        if (!sub.getUser().getUserId().equals(currentUserId)) {
            throw new com.holytrinity.expenso.shared.exception.NotFoundException();
        }
    }

    @Override
    @Transactional
    public List<GoalDTO> processBulkGoals(String userId, List<GoalDTO> incomingGoals) {
        User user = userPort.loadUser(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<String> incomingIds = incomingGoals.stream().map(GoalDTO::getGoalId).collect(Collectors.toList());
        List<Goal> existingGoals = incomingIds.isEmpty() ? new ArrayList<>() : planPort.findGoalsWithDeletedByIdsAndUserId(incomingIds, userId);
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

        List<String> incomingIds = incomingSubscriptions.stream().map(SubscriptionDTO::getSubscriptionId).collect(Collectors.toList());
        List<Subscription> existingSubscriptions = incomingIds.isEmpty() ? new ArrayList<>() : planPort.findSubscriptionsWithDeletedByIdsAndUserId(incomingIds, userId);
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
