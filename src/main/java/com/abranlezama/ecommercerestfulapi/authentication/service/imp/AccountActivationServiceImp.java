package com.abranlezama.ecommercerestfulapi.authentication.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.event.ActivateAccountEvent;
import com.abranlezama.ecommercerestfulapi.authentication.event.UserCreatedEvent;
import com.abranlezama.ecommercerestfulapi.authentication.model.AccountActivationToken;
import com.abranlezama.ecommercerestfulapi.authentication.repository.AccountActivationTokenRepository;
import com.abranlezama.ecommercerestfulapi.authentication.service.AccountActivationService;
import com.abranlezama.ecommercerestfulapi.exception.ConflictException;
import com.abranlezama.ecommercerestfulapi.exception.NotFoundException;
import com.abranlezama.ecommercerestfulapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.ACCOUNT_ACTIVATION_TOKEN_NOT_FOUND;
import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.ACCOUNT_IS_ACTIVE_ALREADY;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountActivationServiceImp implements AccountActivationService {

    private final UserRepository userRepository;
    private final AccountActivationTokenRepository accountActivationTokenRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    @Override
    @EventListener
    public void createAccountActivationToken(UserCreatedEvent event) {
        AccountActivationToken activationToken = AccountActivationToken.builder()
                .createdAt(Instant.now(clock))
                .user(event.user())
                .token(UUID.randomUUID())
                .build();

        activationToken = accountActivationTokenRepository.save(activationToken);
        applicationEventPublisher.publishEvent(
                new ActivateAccountEvent(
                        event.user().getFirstName(), event.user().getLastName(),
                        activationToken.getToken().toString(), event.user().getEmail())
        );
    }

    @Override
    public void activateCustomerAccount(String token) {
        AccountActivationToken activationToken = accountActivationTokenRepository
                .findByToken(UUID.fromString(token))
                .orElseThrow(() -> new NotFoundException(ACCOUNT_ACTIVATION_TOKEN_NOT_FOUND));

        if (activationToken.getUser().getEnabled()) throw new ConflictException(ACCOUNT_IS_ACTIVE_ALREADY);

        activationToken.getUser().setEnabled(true);;
        userRepository.save(activationToken.getUser());
    }
}
