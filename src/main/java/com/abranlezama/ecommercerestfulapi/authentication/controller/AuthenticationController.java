package com.abranlezama.ecommercerestfulapi.authentication.controller;

import com.abranlezama.ecommercerestfulapi.authentication.dto.ActivateAccountRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.dto.LoginRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegisterRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.service.AuthenticationService;
import com.abranlezama.ecommercerestfulapi.response.HttpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import static com.abranlezama.ecommercerestfulapi.authentication.util.RefreshTokenConstants.REFRESH_TOKEN_EXPIRATION_TIME;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Value("${custom.api-domain}")
    private String domain;

    @PostMapping
    public ResponseEntity<HttpResponse> authenticateUser(@Valid @RequestBody LoginRequestDTO request) {
        Map<String, String> tokens = authenticationService.authenticateCustomer(request);
        return ResponseEntity.ok()
                .header(SET_COOKIE, createUserRefreshTokenCookie(tokens.get("refreshToken")).toString())
                .body(HttpResponse.builder()
                        .status(OK.getReasonPhrase().toLowerCase())
                        .statusCode(OK.value())
                        .result(Map.of("accessToken", tokens.get("accessToken")))
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
    public ResponseEntity<HttpResponse> registerCustomer(@Valid @RequestBody RegisterRequestDTO request,
                                                 UriComponentsBuilder uriComponentsBuilder) {
        long userId = authenticationService.registerCustomer(request);
        UriComponents uriComponents = uriComponentsBuilder.path("/api/v1/users/{id}").buildAndExpand(userId);
        return ResponseEntity.created(URI.create(uriComponents.toUriString()))
                .body(HttpResponse.builder()
                        .status(CREATED.getReasonPhrase().toLowerCase())
                        .statusCode(CREATED.value()).build());
    }

    @PostMapping("/activate-account")
    public ResponseEntity<HttpResponse> activateCustomerAccount(@Valid @RequestBody ActivateAccountRequestDTO request) {
        authenticationService.activateCustomerAccount(request.token());
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .status(OK.getReasonPhrase().toLowerCase())
                        .statusCode(OK.value())
                        .build());
    }
}
