package com.holytrinity.expenso.plan.adapter.in.web;

import com.holytrinity.expenso.plan.application.dto.GoalDTO;
import com.holytrinity.expenso.plan.application.dto.SubscriptionDTO;
import com.holytrinity.expenso.plan.application.port.in.PlanUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plan")
public class PlanController {

    private final PlanUseCase planUseCase;
    private final com.holytrinity.expenso.security.UserContext userContext;

    public PlanController(PlanUseCase planUseCase, com.holytrinity.expenso.security.UserContext userContext) {
        this.planUseCase = planUseCase;
        this.userContext = userContext;
    }

    @org.springframework.web.bind.annotation.GetMapping("/goals")
    public ResponseEntity<org.springframework.data.domain.Page<GoalDTO>> getAllGoals(
            @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(planUseCase.findAllGoals(pageable));
    }

    @org.springframework.web.bind.annotation.GetMapping("/goals/{goalId}")
    public ResponseEntity<GoalDTO> getGoal(@org.springframework.web.bind.annotation.PathVariable(name = "goalId") final String goalId) {
        return ResponseEntity.ok(planUseCase.getGoal(goalId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/goals/{goalId}")
    public ResponseEntity<GoalDTO> updateGoal(
            @org.springframework.web.bind.annotation.PathVariable(name = "goalId") final String goalId, 
            @RequestBody @jakarta.validation.Valid final GoalDTO goalDTO) {
        return ResponseEntity.ok(planUseCase.updateGoal(goalId, goalDTO));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/goals/{goalId}")
    public ResponseEntity<Void> deleteGoal(@org.springframework.web.bind.annotation.PathVariable(name = "goalId") final String goalId) {
        planUseCase.deleteGoal(goalId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/goals/sync")
    public ResponseEntity<List<GoalDTO>> syncGoals(
            @RequestBody List<GoalDTO> incomingGoals) {
        String userId = userContext.getCurrentUserId();
        List<GoalDTO> updatedGoals = planUseCase.processBulkGoals(userId, incomingGoals);
        return ResponseEntity.ok(updatedGoals);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/goals/sync")
    public ResponseEntity<Void> deleteBulkGoals(@RequestBody final List<String> goalIds) {
        planUseCase.deleteBulkGoals(goalIds);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/subscriptions")
    public ResponseEntity<org.springframework.data.domain.Page<SubscriptionDTO>> getAllSubscriptions(
            @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(planUseCase.findAllSubscriptions(pageable));
    }

    @org.springframework.web.bind.annotation.GetMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<SubscriptionDTO> getSubscription(@org.springframework.web.bind.annotation.PathVariable(name = "subscriptionId") final String subscriptionId) {
        return ResponseEntity.ok(planUseCase.getSubscription(subscriptionId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<SubscriptionDTO> updateSubscription(
            @org.springframework.web.bind.annotation.PathVariable(name = "subscriptionId") final String subscriptionId, 
            @RequestBody @jakarta.validation.Valid final SubscriptionDTO subscriptionDTO) {
        return ResponseEntity.ok(planUseCase.updateSubscription(subscriptionId, subscriptionDTO));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscription(@org.springframework.web.bind.annotation.PathVariable(name = "subscriptionId") final String subscriptionId) {
        planUseCase.deleteSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subscriptions/sync")
    public ResponseEntity<List<SubscriptionDTO>> syncSubscriptions(
            @RequestBody List<SubscriptionDTO> incomingSubscriptions) {
        String userId = userContext.getCurrentUserId();
        List<SubscriptionDTO> updatedSubs = planUseCase.processBulkSubscriptions(userId, incomingSubscriptions);
        return ResponseEntity.ok(updatedSubs);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/subscriptions/sync")
    public ResponseEntity<Void> deleteBulkSubscriptions(@RequestBody final List<String> subscriptionIds) {
        planUseCase.deleteBulkSubscriptions(subscriptionIds);
        return ResponseEntity.noContent().build();
    }
}
