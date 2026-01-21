package com.holytrinity.expenso.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @Email(message = "{Valid.user.email}")
    @NotBlank(message = "{NotBlank.user.email}")
    private String email;

    @NotBlank(message = "{NotBlank.user.password}")
    private String password;
}
