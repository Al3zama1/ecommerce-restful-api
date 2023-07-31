package com.abranlezama.ecommercerestfulapi.authentication.event;

public record ActivateAccountEvent(
        String firstName,
        String lastName,
        String token,
        String email
) { }
