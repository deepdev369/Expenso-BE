package com.holytrinity.expenso.split.application.dto;

import com.holytrinity.expenso.split.domain.SettlementStatus;
import com.holytrinity.expenso.split.domain.SplitMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitDTO {
    private String splitId;
    private Long version;
    private Boolean deleted;
    private String title;
    private String description;
    private Double totalAmount;
    private SplitMethod splitMethod;
    private OffsetDateTime splitDate;
    private String creatorUserId;
    private String paidById;
    private String groupId;
    private SettlementStatus settlementStatus;
    private List<SplitMemberDTO> members;
    private OffsetDateTime dateCreated;
    private OffsetDateTime lastUpdated;
}
