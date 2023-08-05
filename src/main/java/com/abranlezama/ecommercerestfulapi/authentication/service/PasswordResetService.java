package com.abranlezama.ecommercerestfulapi.authentication.service;

import com.abranlezama.ecommercerestfulapi.authentication.dto.PasswordResetDTO;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    void resetPassword(PasswordResetDTO reset);
}
