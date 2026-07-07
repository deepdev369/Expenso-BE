package com.holytrinity.expenso.split.application.dto;

import com.holytrinity.expenso.split.domain.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitMemberDTO {
    private String splitMemberId;
    private String associatedUserId; 
    private Double amountOwed;
    private String rationale;
    private SettlementStatus settlementStatus;
    private Double amountSettled;
    private Double amountRemaining;
    private List<RepaymentDTO> repayments;
}
