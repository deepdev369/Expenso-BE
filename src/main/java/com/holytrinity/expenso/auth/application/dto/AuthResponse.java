package com.holytrinity.expenso.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private long expiresIn;
    private boolean isNewUser;
    private com.holytrinity.expenso.user.application.dto.UserDTO user;
}
