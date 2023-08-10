package com.abranlezama.ecommercerestfulapi.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email,
        @Size(min = 8, max = 15) String password,
        @Size(min = 8, max = 15) String verifyPassword
) {
}
