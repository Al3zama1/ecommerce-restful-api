package com.abranlezama.ecommercerestfulapi.authentication.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.event.UserCreatedEvent;
import com.abranlezama.ecommercerestfulapi.authentication.model.AccountActivationToken;
import com.abranlezama.ecommercerestfulapi.authentication.repository.AccountActivationTokenRepository;
import com.abranlezama.ecommercerestfulapi.exception.ConflictException;
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
import java.util.Optional;
import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.ACCOUNT_ACTIVATION_TOKEN_NOT_FOUND;
import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.ACCOUNT_IS_ACTIVE_ALREADY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("account activation service")
class AccountActivationServiceImpTest {

    @Mock
    private AccountActivationTokenRepository accountActivationTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private Clock clock;
    @InjectMocks
    private AccountActivationServiceImp cut;

    @Nested
    @DisplayName("account activation token creation")
    class AccountActivationTokenCreation {

        @Test
        @DisplayName("should create account activation token and assign it to user")
        void shouldCreateAccountActivationTokenAndAssignItToUser() {
            // Given
            User user = UserObjectMother.customer().build();
            ArgumentCaptor<AccountActivationToken> tokenArgumentCaptor = ArgumentCaptor.forClass(AccountActivationToken.class);

            given(accountActivationTokenRepository.save(any(AccountActivationToken.class))).willAnswer(invocation -> {
                return invocation.getArgument(0);
            });

            // When
            cut.createAccountActivationToken(new UserCreatedEvent(user));

            // Then
            then(accountActivationTokenRepository).should().save(tokenArgumentCaptor.capture());
            AccountActivationToken savedToken = tokenArgumentCaptor.getValue();

            assertThat(savedToken.getUser()).isEqualTo(user);
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
