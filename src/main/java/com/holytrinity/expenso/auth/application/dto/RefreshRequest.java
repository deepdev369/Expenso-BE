package com.holytrinity.expenso.auth.application.dto;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
