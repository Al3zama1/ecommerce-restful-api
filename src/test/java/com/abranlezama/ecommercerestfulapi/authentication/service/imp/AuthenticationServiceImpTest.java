package com.abranlezama.ecommercerestfulapi.authentication.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.dto.LoginRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegisterRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.model.AccountActivationToken;
import com.abranlezama.ecommercerestfulapi.authentication.repository.AccountActivationTokenRepository;
import com.abranlezama.ecommercerestfulapi.exception.BadRequestException;
import com.abranlezama.ecommercerestfulapi.exception.ConflictException;
import com.abranlezama.ecommercerestfulapi.exception.NotFoundException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
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
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("John", "Last",
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
        }

        @Test
        @DisplayName("should fail registration and throw BadRequestException when registration passwords do not match")
        void shouldFailRegistrationAndThrowBadRequestExceptionWhenRegistrationPasswordsDoNotMatch() {
            // Given
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("John", "Last",
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
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("John", "Last",
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
            LoginRequestDTO loginRequest = new LoginRequestDTO("john.last@gmail.com", "12345678");
            SecurityService.UserPrincipal userPrincipal = new SecurityService.UserPrincipal(UserObjectMother.customer().build());
            UsernamePasswordAuthenticationToken authTokenRequest = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
            UsernamePasswordAuthenticationToken authTokenResponse = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null
            );

            given(authenticationManager.authenticate(authTokenRequest)).willReturn(authTokenResponse);
            given(jwtService.createAccessToken(userPrincipal)).willReturn("access-token");
            given(jwtService.createRefreshToken()).willReturn("refresh-token");

            // When
            Map<String, String> tokens = cut.authenticateCustomer(loginRequest);

            // Then
            assertThat(tokens.get("accessToken")).isEqualTo("access-token");
            assertThat(tokens.get("refreshToken")).isEqualTo("refresh-token");
        }
    }

    @Nested
    @DisplayName("customer account activation")
    class CustomerAccountActivation {

        @Test
        @DisplayName("should activate customer account")
        void shouldActivateCustomerAccount() {
            // Given
            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            String token = UUID.randomUUID().toString();
            AccountActivationToken activationToken = new AccountActivationToken(
                    1L, UUID.fromString(token), Instant.now(clock), UserObjectMother.customer().build()
            );

            given(accountActivationTokenRepository.findByToken(UUID.fromString(token)))
                    .willReturn(Optional.of(activationToken));

            // When
            cut.activateCustomerAccount(token);

            // Then
            then(userRepository).should().save(userArgumentCaptor.capture());
            User savedUser = userArgumentCaptor.getValue();

            assertThat(savedUser.getEnabled()).isEqualTo(true);
        }

        @Test
        @DisplayName("should throw NotFoundException when activation token is not found")
        void shouldThrowNotFoundExceptionWhenActivationTokenIsNotFound() {
            // Given
            String token = UUID.randomUUID().toString();

            given(accountActivationTokenRepository.findByToken(UUID.fromString(token)))
                    .willReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> cut.activateCustomerAccount(token))
                    .hasMessage(ACCOUNT_ACTIVATION_TOKEN_NOT_FOUND)
                    .isInstanceOf(NotFoundException.class);

            // Then
            then(userRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("should throw ConflictException when activating active account")
        void shouldThrowConflictExceptionWhenActivatingActiveAccount() {
            // Given
            String token = UUID.randomUUID().toString();
            AccountActivationToken activationToken = new AccountActivationToken(
                    1L, UUID.fromString(token), Instant.now(clock), UserObjectMother.customer().enabled(true).build()
            );

            given(accountActivationTokenRepository.findByToken(UUID.fromString(token)))
                    .willReturn(Optional.of(activationToken));

            // When
            assertThatThrownBy(() -> cut.activateCustomerAccount(token))
                    .hasMessage(ACCOUNT_IS_ACTIVE_ALREADY)
                    .isInstanceOf(ConflictException.class);

            // Then
            then(userRepository).shouldHaveNoInteractions();
        }
    }


}
