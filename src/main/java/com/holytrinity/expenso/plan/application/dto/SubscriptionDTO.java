package com.holytrinity.expenso.plan.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private String subscriptionId;
    private Long version;
    private Boolean deleted;
    private String name;
    private String merchant;
    private Double amount;
    private String renewalDate;
    private Boolean isActive;
    private String colorHex;
    private String userId;
    private Long dateCreated;
    private Long lastUpdated;
}
