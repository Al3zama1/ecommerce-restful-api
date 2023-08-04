package com.abranlezama.ecommercerestfulapi.authentication.event;

public record ResetPasswordEvent(
        String email,
        String token
) { }
