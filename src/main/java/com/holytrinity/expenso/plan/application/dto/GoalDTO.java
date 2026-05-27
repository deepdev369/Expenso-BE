package com.holytrinity.expenso.plan.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalDTO {
    private String goalId;
    private Long version;
    private Boolean deleted;
    private String name;
    private Double currentAmount;
    private Double targetAmount;
    private String projectedDate;
    private String colorHex;
    private Boolean isCompleted;
    private String aiSuggestion;
    private String userId;
    private Long dateCreated;
    private Long lastUpdated;
}
