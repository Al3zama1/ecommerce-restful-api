package com.abranlezama.ecommercerestfulapi.authentication.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.model.PasswordResetToken;
import com.abranlezama.ecommercerestfulapi.authentication.repository.PasswordResetTokenRepository;
import com.abranlezama.ecommercerestfulapi.exception.ForbiddenException;
import com.abranlezama.ecommercerestfulapi.exception.NotFoundException;
import com.abranlezama.ecommercerestfulapi.objectMother.UserObjectMother;
import com.abranlezama.ecommercerestfulapi.user.model.User;
import com.abranlezama.ecommercerestfulapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.ACCOUNT_MUST_BE_ENABLED_TO_RESET_PASSWORD;
import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.PASSWORD_RESET_REQUEST_FOR_NON_EXISTING_USER;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("password reset service")
class PasswordResetServiceImpTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Clock clock;
    @InjectMocks
    private PasswordResetServiceImp cut;

    @Nested
    @DisplayName("request password reset")
    class RequestPasswordReset {

        @Test
        @DisplayName("should create password reset token")
        void shouldCreatePasswordResetToken() {
            // Given
            String email = "john.last@gmail.com";
            User user = UserObjectMother.customer().enabled(true).build();
            ArgumentCaptor<PasswordResetToken> passwordTokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            LocalDateTime defaultTime = LocalDateTime.of(2022, 11, 9, 10, 11);
            Instant instant = defaultTime.toInstant(ZoneOffset.UTC);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(clock.instant()).willReturn(instant);
            given(passwordResetTokenRepository.save(any(PasswordResetToken.class))).willAnswer(invocation -> {
                return invocation.getArgument(0);
            });

            // When
            cut.requestPasswordReset(email);

            // Then
            then(passwordResetTokenRepository).should().save(passwordTokenCaptor.capture());
            PasswordResetToken savedToken = passwordTokenCaptor.getValue();

            assertThat(savedToken.getUser().getEmail()).isEqualTo(email);
            assertThat(savedToken.getCreatedAt()).isEqualTo(instant);
            assertThat(savedToken.getExpiresAt().minus(1, HOURS)).isEqualTo(instant);
        }

        @Test
        @DisplayName("should throw NotFoundException when attempting to reset password for non existing user")
        void shouldThrowNotFoundExceptionWhenAttemptingToResetPasswordForNonExistingUser() {
            // Given
            String email = "john.last@gmail.com";

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> cut.requestPasswordReset(email))
                    .hasMessage(PASSWORD_RESET_REQUEST_FOR_NON_EXISTING_USER)
                    .isInstanceOf(NotFoundException.class);

            // Then
            then(passwordResetTokenRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("should throw ForbiddenException when attempting to reset password for unactivated account ")
        void shouldThrowForbiddenExceptionWhenAttemptingToResetPasswordForUnactivatedAccount() {
            // Given
            String email = "john.last@gmail.com";
            User user = UserObjectMother.customer().build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            // When
            assertThatThrownBy(() -> cut.requestPasswordReset(email))
                    .hasMessage(ACCOUNT_MUST_BE_ENABLED_TO_RESET_PASSWORD)
                    .isInstanceOf(ForbiddenException.class);

            // Then
            then(passwordResetTokenRepository).shouldHaveNoInteractions();
        }
    }


}
