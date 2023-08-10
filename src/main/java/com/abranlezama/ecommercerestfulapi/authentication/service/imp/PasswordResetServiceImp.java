package com.abranlezama.ecommercerestfulapi.authentication.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.dto.PasswordResetRequest;
import com.abranlezama.ecommercerestfulapi.authentication.event.ResetPasswordEvent;
import com.abranlezama.ecommercerestfulapi.authentication.model.PasswordResetToken;
import com.abranlezama.ecommercerestfulapi.authentication.repository.PasswordResetTokenRepository;
import com.abranlezama.ecommercerestfulapi.authentication.service.PasswordResetService;
import com.abranlezama.ecommercerestfulapi.exception.BadRequestException;
import com.abranlezama.ecommercerestfulapi.exception.ForbiddenException;
import com.abranlezama.ecommercerestfulapi.exception.NotFoundException;
import com.abranlezama.ecommercerestfulapi.user.model.User;
import com.abranlezama.ecommercerestfulapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.*;
import static java.time.temporal.ChronoUnit.HOURS;

@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetServiceImp implements PasswordResetService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(PASSWORD_RESET_REQUEST_FOR_NON_EXISTING_USER));

        if (!user.getEnabled()) throw new ForbiddenException(ACCOUNT_MUST_BE_ENABLED_TO_RESET_PASSWORD);

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(UUID.randomUUID())
                .user(user)
                .createdAt(Instant.now(clock))
                .expiresAt(Instant.now(clock).plus(1, HOURS))
                .build();

        passwordResetToken = passwordResetTokenRepository.save(passwordResetToken);
        applicationEventPublisher.publishEvent(new ResetPasswordEvent(user.getEmail(), passwordResetToken.getToken().toString()));
    }

    @Override
    public void resetPassword(PasswordResetRequest request) {
        if (!request.password().equals(request.verifyPassword())) {
            throw new BadRequestException(RESET_PASSWORDS_MUST_MATCH);
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new NotFoundException(PASSWORD_RESET_TOKEN_NOT_FOUND));

        if (!passwordResetToken.getExpiresAt().isAfter(Instant.now(clock)))
            throw new ForbiddenException(PASSWORD_RESET_TOKEN_HAS_EXPIRED);

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }
}
