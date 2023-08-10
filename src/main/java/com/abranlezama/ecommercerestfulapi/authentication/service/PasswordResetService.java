package com.abranlezama.ecommercerestfulapi.authentication.service;

import com.abranlezama.ecommercerestfulapi.authentication.dto.PasswordResetRequest;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    void resetPassword(PasswordResetRequest reset);
}
