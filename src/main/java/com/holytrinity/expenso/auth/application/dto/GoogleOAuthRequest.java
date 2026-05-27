package com.holytrinity.expenso.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleOAuthRequest {
    @NotBlank
    private String idToken;
}
