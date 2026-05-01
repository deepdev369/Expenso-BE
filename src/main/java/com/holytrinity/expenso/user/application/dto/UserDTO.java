package com.holytrinity.expenso.user.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private String userId;

    private Long version;

    @NotNull
    @Size(max = 255)
    @UserEmailUnique
    private String email;

    @NotNull
    @Size(max = 255)
    private String userName;

    @Size(max = 255)
    private String phone;

    private List<@Size(max = 255) String> authProviders;

    @Size(max = 255)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    @NotNull
    private Boolean emailVerified;

    @NotNull
    @Size(max = 255)
    private String defaultCurrency;

    @NotNull
    @Size(max = 255)
    private String language;

    private java.util.Map<String, java.util.List<String>> categoriesMapping;

    private java.util.List<String> paymentMethods;

    @NotNull
    private Boolean smsConsentGranted;

    @NotNull
    private Boolean voiceConsentGranted;

    private Long consentGrantedAt;

    private Long lastLoginAt;

}
