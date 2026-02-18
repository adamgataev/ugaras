package com.amac.ugaras.models.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank(message = "Email is verplicht")
        @Email(message = "Ongeldig email formaat")
        String email,
        @NotBlank(message = "Wachtwoord is verplicht")
        @Size(min = 8, message = "Wachtwoord moet minimaal 8 tekens zijn")
        String password
) {}