package com.amac.ugaras.models.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @Size(max = 20, message = "{user.phone.size}")
        String phoneNumber,

        @Email(message = "{user.email.invalid}")
        @Size(max = 255)
        String email,

        @Size(min = 8, message = "{user.password.size}")
        String password,

        Boolean enabled
) {}
