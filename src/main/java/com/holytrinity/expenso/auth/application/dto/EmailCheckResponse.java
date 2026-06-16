package com.holytrinity.expenso.auth.application.dto;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class EmailCheckResponse {
    private boolean exists;
    private List<String> authProviders;
}
