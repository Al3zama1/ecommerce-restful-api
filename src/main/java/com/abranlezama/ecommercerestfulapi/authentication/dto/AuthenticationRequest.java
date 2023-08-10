package com.abranlezama.ecommercerestfulapi.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @Email String email,
        @Size(min = 8, max = 15) String password
) {
}
