package com.abranlezama.ecommercerestfulapi.authentication.controller;

import com.abranlezama.ecommercerestfulapi.authentication.dto.LoginRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegisterRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.service.AuthenticationService;
import com.abranlezama.ecommercerestfulapi.config.CorsConfig;
import com.abranlezama.ecommercerestfulapi.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("John", "Last",
                    "john.last@gmail.com", "12345678", "12345678"
            );

            given(authenticationService.registerCustomer(registerRequest)).willReturn(1L);

            // When
            mockMvc.perform(post("/api/v1/auth/register")
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
                    .andExpect(jsonPath("$.message", Matchers.is(FAILED_AUTHENTICATION)))
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
                    .andExpect(jsonPath("$.message", Matchers.is(FAILED_AUTHENTICATION)))
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
                    .andExpect(jsonPath("$.message", Matchers.is(ACCOUNT_DISABLED)))
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
                    .andExpect(jsonPath("$.message", Matchers.is(ACCOUNT_LOCKED)))
                    .andExpect(jsonPath("$.status", Matchers.is(UNAUTHORIZED.getReasonPhrase())))
                    .andExpect(jsonPath("$.statusCode", Matchers.is(UNAUTHORIZED.value())));

            // Then
            then(authenticationService).should().authenticateCustomer(loginRequest);

        }
    }

}
