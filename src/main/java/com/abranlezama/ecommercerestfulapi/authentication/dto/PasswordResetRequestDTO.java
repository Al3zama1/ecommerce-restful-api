package com.abranlezama.ecommercerestfulapi.authentication.dto;

import jakarta.validation.constraints.Email;

public record PasswordResetRequestDTO(
        @Email String email
) {
}
