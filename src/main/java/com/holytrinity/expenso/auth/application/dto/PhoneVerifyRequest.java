package com.holytrinity.expenso.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhoneVerifyRequest {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String otp;
}
