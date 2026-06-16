package com.holytrinity.expenso.plan.adapter.out.persistence;

import com.holytrinity.expenso.plan.application.port.out.PlanPort;
import com.holytrinity.expenso.plan.domain.Goal;
import com.holytrinity.expenso.plan.domain.Subscription;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlanPersistenceAdapter implements PlanPort {

    private final SpringDataGoalRepository goalRepository;
    private final SpringDataSubscriptionRepository subscriptionRepository;

    public PlanPersistenceAdapter(SpringDataGoalRepository goalRepository, SpringDataSubscriptionRepository subscriptionRepository) {
        this.goalRepository = goalRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public org.springframework.data.domain.Page<Goal> findAllGoals(org.springframework.data.domain.Pageable pageable) {
        return goalRepository.findAll(pageable);
    }

    @Override
    public java.util.Optional<Goal> loadGoal(String goalId) {
        return goalRepository.findById(goalId);
    }

    @Override
    public void deleteGoal(Goal goal) {
        goalRepository.delete(goal);
    }

    @Override
    public org.springframework.data.domain.Page<Subscription> findAllSubscriptions(org.springframework.data.domain.Pageable pageable) {
        return subscriptionRepository.findAll(pageable);
    }

    @Override
    public java.util.Optional<Subscription> loadSubscription(String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId);
    }

    @Override
    public void deleteSubscription(Subscription subscription) {
        subscriptionRepository.delete(subscription);
    }

    @Override
    public List<Goal> saveAllGoals(List<Goal> goals) {
        return goalRepository.saveAll(goals);
    }

    @Override
    public List<Subscription> saveAllSubscriptions(List<Subscription> subscriptions) {
        return subscriptionRepository.saveAll(subscriptions);
    }

    @Override
    public List<Goal> findGoalsByUserId(String userId) {
        return goalRepository.findByUser_UserId(userId);
    }

    @Override
    public List<Subscription> findSubscriptionsByUserId(String userId) {
        return subscriptionRepository.findByUser_UserId(userId);
    }

    @Override
    public List<Goal> findGoalsWithDeletedByIdsAndUserId(List<String> ids, String userId) {
        return goalRepository.findAllWithDeletedByIdsAndUserId(ids, userId);
    }

    @Override
    public List<Subscription> findSubscriptionsWithDeletedByIdsAndUserId(List<String> ids, String userId) {
        return subscriptionRepository.findAllWithDeletedByIdsAndUserId(ids, userId);
    }
}
