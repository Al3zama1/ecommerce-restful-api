package com.abranlezama.ecommercerestfulapi.authentication.dto;

import jakarta.validation.constraints.Size;

public record ActivateAccountRequestDTO(
        @Size(min = 36, max = 36) String token
) {
}
