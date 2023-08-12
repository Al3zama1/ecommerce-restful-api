package com.abranlezama.ecommercerestfulapi.authentication.controller;

import com.abranlezama.ecommercerestfulapi.authentication.dto.AccountActivationRequest;
import com.abranlezama.ecommercerestfulapi.authentication.dto.AuthenticationRequest;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegistrationRequest;
import com.abranlezama.ecommercerestfulapi.authentication.service.AccountActivationService;
import com.abranlezama.ecommercerestfulapi.authentication.service.AuthenticationService;
import com.abranlezama.ecommercerestfulapi.jwt.service.JwtService;
import com.abranlezama.ecommercerestfulapi.response.HttpResponse;
import com.abranlezama.ecommercerestfulapi.user.service.imp.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.authentication.util.RefreshTokenConstants.REFRESH_TOKEN_EXPIRATION_TIME;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AccountActivationService accountActivationService;
    private final JwtService jwtService;
    private final CacheManager cacheManager;

    @Value("${custom.api-domain}")
    private String domain;

    @PostMapping
    public ResponseEntity<HttpResponse> authenticateUser(@Valid @RequestBody AuthenticationRequest request) {
        SecurityService.UserPrincipal userDetails = (SecurityService.UserPrincipal) authenticationService.authenticateCustomer(request);
        String accessToken = jwtService.createAccessToken(userDetails);
        String refreshToken = authenticationService.createRefreshToken(userDetails);

        return ResponseEntity.ok()
                .header(SET_COOKIE, createUserRefreshTokenCookie(refreshToken).toString())
                .body(HttpResponse.builder()
                        .status(OK.getReasonPhrase().toLowerCase())
                        .statusCode(OK.value())
                        .result(Map.of("accessToken", accessToken))
                        .build()
                );
    }

    private ResponseCookie createUserRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .sameSite("Strict")
                .domain(domain)
                .path("/api/v1/auth/refresh-token")
                .maxAge(Duration.ofMillis(REFRESH_TOKEN_EXPIRATION_TIME))
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> registerCustomer(@Valid @RequestBody RegistrationRequest request,
                                                         @RequestHeader(name = "idempotency-key") UUID idempotencyKey,
                                                 UriComponentsBuilder uriComponentsBuilder) {

        Cache idempotencyCache = cacheManager.getCache("idempotency");
        Long userId = idempotencyCache.get(idempotencyKey.toString(), Long.class);

        if (userId == null) {
            userId = authenticationService.registerCustomer(request);
            idempotencyCache.put(idempotencyKey.toString(), userId);
        }

        UriComponents uriComponents = uriComponentsBuilder.path("/api/v1/users/{id}").buildAndExpand(userId);

        return ResponseEntity.created(URI.create(uriComponents.toUriString()))
                .body(HttpResponse.builder()
                        .status(CREATED.getReasonPhrase().toLowerCase())
                        .statusCode(CREATED.value()).build());
    }

    @PostMapping("/activate-account")
    public ResponseEntity<HttpResponse> activateCustomerAccount(@Valid @RequestBody AccountActivationRequest request) {
        accountActivationService.activateCustomerAccount(request.token());
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .status(OK.getReasonPhrase().toLowerCase())
                        .statusCode(OK.value())
                        .build());
    }
}
