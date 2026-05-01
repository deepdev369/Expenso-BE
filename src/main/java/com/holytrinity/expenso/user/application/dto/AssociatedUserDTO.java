package com.holytrinity.expenso.user.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssociatedUserDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String clientReferenceId;

    @NotNull
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String phone;

    @Size(max = 255)
    private String email;

    @NotNull
    private Long userId;

    private Long version;
}
