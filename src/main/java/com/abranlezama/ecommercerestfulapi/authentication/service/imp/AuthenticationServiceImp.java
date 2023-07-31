package com.abranlezama.ecommercerestfulapi.authentication.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.dto.LoginRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegisterRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.model.AccountActivationToken;
import com.abranlezama.ecommercerestfulapi.authentication.repository.AccountActivationTokenRepository;
import com.abranlezama.ecommercerestfulapi.authentication.service.AuthenticationService;
import com.abranlezama.ecommercerestfulapi.exception.BadRequestException;
import com.abranlezama.ecommercerestfulapi.exception.ConflictException;
import com.abranlezama.ecommercerestfulapi.exception.NotFoundException;
import com.abranlezama.ecommercerestfulapi.jwt.service.JwtService;
import com.abranlezama.ecommercerestfulapi.user.model.User;
import com.abranlezama.ecommercerestfulapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.exception.ExceptionMessages.*;
import static com.abranlezama.ecommercerestfulapi.user.role.UserRoleType.CUSTOMER;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationServiceImp implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AccountActivationTokenRepository accountActivationTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public long registerCustomer(RegisterRequestDTO request) {
        if (!request.password().equals(request.verifyPassword()))
            throw new BadRequestException(REGISTER_PASSWORDS_MISMATCH);

        boolean emailTaken = userRepository.existsByEmail(request.email());
        if (emailTaken) throw new ConflictException(REGISTER_EMAIL_MUST_BE_UNIQUE);

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(CUSTOMER)
                .build();
        user = userRepository.save(user);

        return user.getId();
    }

    @Override
    public Map<String, String> authenticateCustomer(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Map.of(
                "accessToken", jwtService.createAccessToken(userDetails),
                "refreshToken", jwtService.createRefreshToken()
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
