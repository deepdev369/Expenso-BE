package com.holytrinity.expenso.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailAuthRequest {
    @NotBlank
    @Email
    private String email;
}
