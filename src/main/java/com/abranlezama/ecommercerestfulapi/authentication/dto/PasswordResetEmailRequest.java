package com.abranlezama.ecommercerestfulapi.authentication.dto;

import jakarta.validation.constraints.Email;

public record PasswordResetEmailRequest(
        @Email String email
) {
}
