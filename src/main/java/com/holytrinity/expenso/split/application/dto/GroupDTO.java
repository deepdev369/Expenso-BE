package com.holytrinity.expenso.split.application.dto;

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
public class GroupDTO {
    private String id;
    private String name;
    private String description;
    private List<String> memberIds;
    private Boolean deleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
