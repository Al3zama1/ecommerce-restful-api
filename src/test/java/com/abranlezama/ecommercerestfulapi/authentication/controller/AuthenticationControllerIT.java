package com.abranlezama.ecommercerestfulapi.authentication.controller;

import com.abranlezama.ecommercerestfulapi.authentication.dto.ActivateAccountRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.dto.LoginRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegisterRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.service.AccountActivationService;
import com.abranlezama.ecommercerestfulapi.authentication.service.AuthenticationService;
import com.abranlezama.ecommercerestfulapi.config.CorsConfig;
import com.abranlezama.ecommercerestfulapi.config.SecurityConfig;
import com.abranlezama.ecommercerestfulapi.exception.ConflictException;
import com.abranlezama.ecommercerestfulapi.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
@DisplayName("authentication controller")
class AuthenticationControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private CacheManager cacheManager;
    @MockBean
    private AccountActivationService accountActivationService;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private CorsConfig corsConfig;

    @Nested
    @DisplayName("customer registration")
    class Customer {

        @Test
        @DisplayName("return 201 status code when customer is registered")
        void shouldReturn201StatusCodeWhenCustomerIsRegistered() throws Exception {
            // Given
            UUID idempotencyKey = UUID.randomUUID();
            Cache cache = Mockito.mock(Cache.class);
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("John", "Last",
                    "john.last@gmail.com", "12345678", "12345678"
            );

            given(cacheManager.getCache("idempotency")).willReturn(cache);
            given(authenticationService.registerCustomer(registerRequest)).willReturn(1L);

            // When
            mockMvc.perform(post("/api/v1/auth/register")
                    .header("idempotency-key", idempotencyKey)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", Matchers.containsString("/users/1")));

            // Then
            then(authenticationService).should().registerCustomer(registerRequest);
        }

        @Test
        @DisplayName("return 401 status code when registration input is invalid")
        void shouldReturn401StatusCodeWhenRegistrationInputIsInvalid() throws Exception {
            // Given
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("John", "Last",
                    "john.last@gmail.com", "123456", "12345678"
            );

            // When
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().doesNotExist("Location"));

            // Then
            then(authenticationService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("customer authentication")
    class CustomerAuthentication {

        @Test
        @DisplayName("return 200 status code when authentication succeeds")
        void shouldReturn200StatusCodeWhenAuthenticationSucceeds() throws Exception {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO("john.last@gmail.com", "12345678");

            given(authenticationService.authenticateCustomer(loginRequest))
                    .willReturn(Map.of("refreshToken", "refresh-token", "accessToken", "access-token"));

            // When
            mockMvc.perform(post("/api/v1/auth")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("refreshToken"))
                    .andExpect(cookie().maxAge("refreshToken", 432000));

            // Then
            then(authenticationService).should().authenticateCustomer(loginRequest);

        }

        @Test
        @DisplayName("return 401 status cod when authentication fails due to incorrect email")
        void shouldReturn401StatusCodeWhenCustomerAuthenticationFailsDueToIncorrectEmail() throws Exception {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO("john.last@gmail.com", "12345678");

            given(authenticationService.authenticateCustomer(loginRequest))
                    .willThrow(new UsernameNotFoundException(FAILED_AUTHENTICATION));

            // When
            mockMvc.perform(post("/api/v1/auth")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorMessage", Matchers.is(FAILED_AUTHENTICATION)))
                    .andExpect(jsonPath("$.status", Matchers.is(UNAUTHORIZED.getReasonPhrase())))
                    .andExpect(jsonPath("$.statusCode", Matchers.is(UNAUTHORIZED.value())));

            // Then
            then(authenticationService).should().authenticateCustomer(loginRequest);

        }

        @Test
        @DisplayName("return 401 status cod when authentication fails due to incorrect password")
        void shouldReturn401StatusCodeWhenCustomerAuthenticationFailsDueToIncorrectPassword() throws Exception {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO("john.last@gmail.com", "12345678");

            given(authenticationService.authenticateCustomer(loginRequest))
                    .willThrow(new BadCredentialsException(FAILED_AUTHENTICATION));

            // When
            mockMvc.perform(post("/api/v1/auth")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorMessage", Matchers.is(FAILED_AUTHENTICATION)))
                    .andExpect(jsonPath("$.status", Matchers.is(UNAUTHORIZED.getReasonPhrase())))
                    .andExpect(jsonPath("$.statusCode", Matchers.is(UNAUTHORIZED.value())));

            // Then
            then(authenticationService).should().authenticateCustomer(loginRequest);

        }

        @Test
        @DisplayName("return 401 status cod when authentication fails due to unactivated account")
        void shouldReturn401StatusCodeWhenCustomerAuthenticationFailsDueToUnactivatedAccount() throws Exception {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO("john.last@gmail.com", "12345678");

            given(authenticationService.authenticateCustomer(loginRequest))
                    .willThrow(new DisabledException(ACCOUNT_DISABLED));

            // When
            mockMvc.perform(post("/api/v1/auth")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorMessage", Matchers.is(ACCOUNT_DISABLED)))
                    .andExpect(jsonPath("$.status", Matchers.is(UNAUTHORIZED.getReasonPhrase())))
                    .andExpect(jsonPath("$.statusCode", Matchers.is(UNAUTHORIZED.value())));

            // Then
            then(authenticationService).should().authenticateCustomer(loginRequest);

        }

        @Test
        @DisplayName("return 401 status cod when authentication fails due to account locked")
        void shouldReturn401StatusCodeWhenCustomerAuthenticationFailsDueToAccountLocked() throws Exception {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO("john.last@gmail.com", "12345678");

            given(authenticationService.authenticateCustomer(loginRequest))
                    .willThrow(new LockedException(ACCOUNT_LOCKED));

            // When
            mockMvc.perform(post("/api/v1/auth")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorMessage", Matchers.is(ACCOUNT_LOCKED)))
                    .andExpect(jsonPath("$.status", Matchers.is(UNAUTHORIZED.getReasonPhrase())))
                    .andExpect(jsonPath("$.statusCode", Matchers.is(UNAUTHORIZED.value())));

            // Then
            then(authenticationService).should().authenticateCustomer(loginRequest);

        }
    }

    @Nested
    @DisplayName("customer account activation")
    class CustomerAccountActivation {

        @Test
        @DisplayName("should return 200 status code when account customer is activate")
        void shouldReturn200StatusCodeWhenCustomerAccountIsActivated() throws Exception {
            // Given
            String activationToken = UUID.randomUUID().toString();
            ActivateAccountRequestDTO activateRequest = new ActivateAccountRequestDTO(activationToken);

            // When
            mockMvc.perform(post("/api/v1/auth/activate-account")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(activateRequest)))
                    .andExpect(status().isOk());

            // Then
            then(accountActivationService).should().activateCustomerAccount(activationToken);
        }

        @Test
        @DisplayName("should return 400 status code when activation token fails validation")
        void shouldReturn400StatusCodeWhenActivationTokenFailsValidation() throws Exception {
            // Given
            String activationToken = "some random incorrect account activation token";
            ActivateAccountRequestDTO activateRequest = new ActivateAccountRequestDTO(activationToken);

            // When
            mockMvc.perform(post("/api/v1/auth/activate-account")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(activateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors[0].field", Matchers.is("token")))
                    .andExpect(jsonPath("$.validationErrors[0].message", Matchers.is("size must be between 36 and 36")));

            // Then
            then(authenticationService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("should return 404 status when account activation token is not valid")
        void shouldReturn404StatusWhenAccountActivationTokenIsNotValid() throws Exception {
            // Given
            String activationToken = UUID.randomUUID().toString();
            ActivateAccountRequestDTO activateRequest = new ActivateAccountRequestDTO(activationToken);

            doThrow(new NotFoundException(ACCOUNT_ACTIVATION_TOKEN_NOT_FOUND)).when(accountActivationService)
                    .activateCustomerAccount(activationToken);

            // When
            mockMvc.perform(post("/api/v1/auth/activate-account")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(activateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorMessage", Matchers.is(ACCOUNT_ACTIVATION_TOKEN_NOT_FOUND)));

            // Then
            then(accountActivationService).should().activateCustomerAccount(activationToken);
        }

        @Test
        @DisplayName("should return 409 status when activating active account")
        void shouldReturn409StatusCodeWhenActivatingActiveAccount() throws Exception {
            // Given
            String activationToken = UUID.randomUUID().toString();
            ActivateAccountRequestDTO activateRequest = new ActivateAccountRequestDTO(activationToken);

            doThrow(new ConflictException(ACCOUNT_IS_ACTIVE_ALREADY)).when(accountActivationService)
                    .activateCustomerAccount(activationToken);

            // When
            mockMvc.perform(post("/api/v1/auth/activate-account")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(activateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorMessage", Matchers.is(ACCOUNT_IS_ACTIVE_ALREADY)));

            // Then
            then(accountActivationService).should().activateCustomerAccount(activationToken);
        }
    }

}
