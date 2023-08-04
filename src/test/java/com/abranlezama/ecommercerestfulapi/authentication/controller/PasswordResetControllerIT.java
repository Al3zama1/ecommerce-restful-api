package com.abranlezama.ecommercerestfulapi.authentication.controller;

import com.abranlezama.ecommercerestfulapi.authentication.dto.PasswordResetRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.service.PasswordResetService;
import com.abranlezama.ecommercerestfulapi.config.CorsConfig;
import com.abranlezama.ecommercerestfulapi.config.SecurityConfig;
import com.abranlezama.ecommercerestfulapi.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.PASSWORD_RESET_REQUEST_FOR_NON_EXISTING_USER;
import static com.abranlezama.ecommercerestfulapi.response.ResponseMessage.PASSWORD_RESET_REQUEST;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PasswordResetController.class)
@Import(SecurityConfig.class)
@DisplayName("password reset controller")
class PasswordResetControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private CorsConfig corsConfig;
    @MockBean
    private PasswordResetService passwordResetService;

    @Nested
    @DisplayName("password reset token generation")
    class PasswordResetTokenGeneration {

        @Test
        @DisplayName("should return 200 status code when password reset token is generated")
        void shouldReturn204StatusCodeWhenPasswordResetTokenIsGenerated() throws Exception {
            // Given
            PasswordResetRequestDTO request = new PasswordResetRequestDTO("john.last@gmail.com");

            // When
            mockMvc.perform(post("/api/v1/auth/reset-password")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", Matchers.is(PASSWORD_RESET_REQUEST)));

            // Then
            then(passwordResetService).should().requestPasswordReset(request.email());
        }

        @Test
        @DisplayName("return 404 status code when password reset request is made for non existing email")
        void shouldReturn404StatusCodeWhenPasswordResetRequestIsMadeForNonExistingEmail() throws Exception {
            // Given
            PasswordResetRequestDTO request = new PasswordResetRequestDTO("john.last@gmail.com");

            doThrow(new NotFoundException(PASSWORD_RESET_REQUEST_FOR_NON_EXISTING_USER)).when(passwordResetService)
                    .requestPasswordReset(request.email());

            // When
            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorMessage", Matchers.is(PASSWORD_RESET_REQUEST_FOR_NON_EXISTING_USER)));

            // Then
            then(passwordResetService).should().requestPasswordReset(request.email());
        }

    }

}