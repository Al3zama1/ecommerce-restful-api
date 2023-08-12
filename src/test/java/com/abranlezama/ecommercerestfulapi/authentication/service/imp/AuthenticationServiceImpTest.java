package com.abranlezama.ecommercerestfulapi.authentication.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.dto.AuthenticationRequest;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegistrationRequest;
import com.abranlezama.ecommercerestfulapi.authentication.model.RefreshToken;
import com.abranlezama.ecommercerestfulapi.authentication.repository.AccountActivationTokenRepository;
import com.abranlezama.ecommercerestfulapi.authentication.repository.RefreshTokenRepository;
import com.abranlezama.ecommercerestfulapi.exception.BadRequestException;
import com.abranlezama.ecommercerestfulapi.exception.ConflictException;
import com.abranlezama.ecommercerestfulapi.jwt.service.JwtService;
import com.abranlezama.ecommercerestfulapi.objectMother.UserObjectMother;
import com.abranlezama.ecommercerestfulapi.user.model.User;
import com.abranlezama.ecommercerestfulapi.user.repository.UserRepository;
import com.abranlezama.ecommercerestfulapi.user.service.imp.SecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.authentication.util.RefreshTokenConstants.REFRESH_TOKEN_EXPIRATION_TIME;
import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.REGISTER_EMAIL_MUST_BE_UNIQUE;
import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.REGISTER_PASSWORDS_MISMATCH;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("authentication service")
class AuthenticationServiceImpTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Clock clock;
    @Mock
    private AccountActivationTokenRepository accountActivationTokenRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationServiceImp cut;

    @Nested
    @DisplayName("customer registration")
    class CustomerRegistration {

        @Test
        @DisplayName("should register customer")
        void shouldRegisterCustomer() {
            // Given
            RegistrationRequest registerRequest = new RegistrationRequest("John", "Last",
                    "john.last@gmail.com", "12345678", "12345678"
            );

            given(userRepository.existsByEmail(registerRequest.email())).willReturn(false);
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(1L);
                return savedUser;
            });

            // When
            long id = cut.registerCustomer(registerRequest);

            // When
            then(userRepository).should().save(any(User.class));
            assertThat(id).isEqualTo(1);
        }

        @Test
        @DisplayName("should fail registration and throw BadRequestException when registration passwords do not match")
        void shouldFailRegistrationAndThrowBadRequestExceptionWhenRegistrationPasswordsDoNotMatch() {
            // Given
            RegistrationRequest registerRequest = new RegistrationRequest("John", "Last",
                    "john.last@gmail.com", "123456788", "12345678"
            );

            // When
            assertThatThrownBy(() -> cut.registerCustomer(registerRequest))
                    .hasMessage(REGISTER_PASSWORDS_MISMATCH)
                    .isInstanceOf(BadRequestException.class);

            // Then
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("should fail registration and throw ConflictException when registration email is taken")
        void shouldFailRegistrationAndThrowConflictExceptionWhenRegistrationEmailIsTaken() {
            // Given
            RegistrationRequest registerRequest = new RegistrationRequest("John", "Last",
                    "john.last@gmail.com", "12345678", "12345678"
            );

            given(userRepository.existsByEmail(registerRequest.email())).willReturn(true);

            // When
            assertThatThrownBy(() -> cut.registerCustomer(registerRequest))
                    .hasMessage(REGISTER_EMAIL_MUST_BE_UNIQUE)
                    .isInstanceOf(ConflictException.class);

            // Then
            then(userRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("customer authentication")
    class CustomerAuthentication {

        @Test
        @DisplayName("should authenticate customer")
        void shouldAuthenticateCustomer() {
            // Given
            AuthenticationRequest loginRequest = new AuthenticationRequest("john.last@gmail.com", "12345678");
            SecurityService.UserPrincipal userPrincipal = new SecurityService.UserPrincipal(UserObjectMother.customer().build());
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal, null);

            given(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.email(), loginRequest.password()
            ))).willReturn(authenticationToken);

            // When
            UserDetails userDetails = cut.authenticateCustomer(loginRequest);

            // Then
            then(authenticationManager).should().authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }

    @Nested
    @DisplayName("generate refresh token")
    class GenerateRefreshToken {

        @Test
        @DisplayName("should generate user refresh token and return it")
        void shouldGenerateUserRefreshTokenAndReturnIt() {
            // Given
            ArgumentCaptor<RefreshToken> refreshTokenArgumentCaptor = ArgumentCaptor.forClass(RefreshToken.class);
            User user = UserObjectMother.customer().build();
            LocalDateTime defaultTime = LocalDateTime.of(2023, 11, 10, 10, 1);
            Instant instant = defaultTime.toInstant(UTC);

            given(clock.instant()).willReturn(instant);
            given(jwtService.createRefreshToken()).willReturn(UUID.randomUUID().toString());
            given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(invocation -> {
                return invocation.getArgument(0);
            });


            // When
            cut.createRefreshToken(user);


            // Then
            then(refreshTokenRepository).should().save(refreshTokenArgumentCaptor.capture());
            RefreshToken savedToken = refreshTokenArgumentCaptor.getValue();

            assertThat(savedToken.getUser()).isEqualTo(user);
            assertThat(savedToken.getCreatedAt()).isEqualTo(defaultTime.toInstant(UTC));
            assertThat(savedToken.getExpiresAt()).isEqualTo(defaultTime.toInstant(UTC).plusMillis(REFRESH_TOKEN_EXPIRATION_TIME));
        }
    }
}
