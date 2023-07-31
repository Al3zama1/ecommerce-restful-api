package com.abranlezama.ecommercerestfulapi.authentication.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.dto.RegisterRequestDTO;
import com.abranlezama.ecommercerestfulapi.exception.BadRequestException;
import com.abranlezama.ecommercerestfulapi.exception.ConflictException;
import com.abranlezama.ecommercerestfulapi.user.model.User;
import com.abranlezama.ecommercerestfulapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.REGISTER_EMAIL_MUST_BE_UNIQUE;
import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.REGISTER_PASSWORDS_MISMATCH;
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


}
