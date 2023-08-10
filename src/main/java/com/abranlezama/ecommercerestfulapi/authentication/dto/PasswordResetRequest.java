package com.abranlezama.ecommercerestfulapi.authentication.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PasswordResetRequest(
        @Size(min = 8, max = 15) String password,
        @Size(min = 8, max = 15) String verifyPassword,
        UUID token
) {
}
