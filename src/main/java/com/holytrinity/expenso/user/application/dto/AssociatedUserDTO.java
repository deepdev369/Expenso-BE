package com.holytrinity.expenso.user.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssociatedUserDTO {

    private String associatedUserId;

    @NotNull
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String phone;

    @Size(max = 255)
    private String email;



    private Long version;
}
