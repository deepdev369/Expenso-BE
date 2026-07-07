package com.holytrinity.expenso.split.application.dto;

import com.holytrinity.expenso.split.domain.RepaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentDTO {
    private String id;
    private String splitMemberId;
    private String splitId;
    private Double amount;
    private RepaymentMethod method;
    private String note;
    private OffsetDateTime repaidAt;
    private OffsetDateTime createdAt;
}
