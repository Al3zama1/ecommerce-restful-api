package com.abranlezama.ecommercerestfulapi.authentication.service;

public interface PasswordResetService {
    void requestPasswordReset(String email);
}
