package com.holytrinity.expenso.split.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitMemberDTO {
    private String splitMemberId;
    private String associatedUserId; // The friend who owes or is owed
    private Double amountOwed;
    private Boolean hasPaid;
}
