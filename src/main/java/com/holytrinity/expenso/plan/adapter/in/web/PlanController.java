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

    public PlanController(PlanUseCase planUseCase) {
        this.planUseCase = planUseCase;
    }

    @PostMapping("/goals/sync")
    public ResponseEntity<List<GoalDTO>> syncGoals(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<GoalDTO> incomingGoals) {
        String userId = jwt.getSubject();
        List<GoalDTO> updatedGoals = planUseCase.processBulkGoals(userId, incomingGoals);
        return ResponseEntity.ok(updatedGoals);
    }

    @PostMapping("/subscriptions/sync")
    public ResponseEntity<List<SubscriptionDTO>> syncSubscriptions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<SubscriptionDTO> incomingSubscriptions) {
        String userId = jwt.getSubject();
        List<SubscriptionDTO> updatedSubs = planUseCase.processBulkSubscriptions(userId, incomingSubscriptions);
        return ResponseEntity.ok(updatedSubs);
    }
}
