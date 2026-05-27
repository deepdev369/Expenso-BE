package com.holytrinity.expenso.split.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitDTO {
    private String splitId;
    private Long version;
    private Boolean deleted;
    private String description;
    private Double totalAmount;
    private String splitMethod;
    private Long splitDate;
    private String creatorUserId;
    private List<SplitMemberDTO> members;
    private Long dateCreated;
    private Long lastUpdated;
}
