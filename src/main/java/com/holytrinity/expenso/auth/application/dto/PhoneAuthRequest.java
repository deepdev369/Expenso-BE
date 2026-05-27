package com.holytrinity.expenso.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhoneAuthRequest {
    @NotBlank
    private String phoneNumber;
}
