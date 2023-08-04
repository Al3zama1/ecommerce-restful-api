package com.abranlezama.ecommercerestfulapi.authentication.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.model.PasswordResetToken;
import com.abranlezama.ecommercerestfulapi.authentication.repository.PasswordResetTokenRepository;
import com.abranlezama.ecommercerestfulapi.authentication.service.PasswordResetService;
import com.abranlezama.ecommercerestfulapi.exception.ForbiddenException;
import com.abranlezama.ecommercerestfulapi.exception.NotFoundException;
import com.abranlezama.ecommercerestfulapi.user.model.User;
import com.abranlezama.ecommercerestfulapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.ACCOUNT_MUST_BE_ENABLED_TO_RESET_PASSWORD;
import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.PASSWORD_RESET_REQUEST_FOR_NON_EXISTING_USER;
import static java.time.temporal.ChronoUnit.HOURS;

@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetServiceImp implements PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(PASSWORD_RESET_REQUEST_FOR_NON_EXISTING_USER));

        // TODO - don't allow users to reset password if account is disabled
        if (!user.getEnabled()) throw new ForbiddenException(ACCOUNT_MUST_BE_ENABLED_TO_RESET_PASSWORD);

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(UUID.randomUUID())
                .user(user)
                .createdAt(Instant.now(clock))
                .expiresAt(Instant.now(clock).plus(1, HOURS))
                .build();

        passwordResetTokenRepository.save(passwordResetToken);
    }
}
