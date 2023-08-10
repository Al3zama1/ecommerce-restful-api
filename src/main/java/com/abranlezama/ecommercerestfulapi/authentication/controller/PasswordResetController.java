package com.abranlezama.ecommercerestfulapi.authentication.controller;

import com.abranlezama.ecommercerestfulapi.authentication.dto.PasswordResetRequest;
import com.abranlezama.ecommercerestfulapi.authentication.dto.PasswordResetEmailRequest;
import com.abranlezama.ecommercerestfulapi.authentication.service.PasswordResetService;
import com.abranlezama.ecommercerestfulapi.response.HttpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.abranlezama.ecommercerestfulapi.response.ResponseMessage.PASSWORD_RESET;
import static com.abranlezama.ecommercerestfulapi.response.ResponseMessage.PASSWORD_RESET_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/auth/reset-password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final CacheManager cacheManager;

    @PostMapping
    public HttpResponse requestPasswordReset(@Valid @RequestBody PasswordResetEmailRequest request) {
        passwordResetService.requestPasswordReset(request.email());
        return HttpResponse.builder()
                .message(PASSWORD_RESET_REQUEST)
                .status(OK.getReasonPhrase().toLowerCase())
                .statusCode(OK.value())
                .build();
    }

    @PatchMapping
    @Cacheable(value = "idempotency", key = "#idempotencyKey")
    public HttpResponse resetPassword(@Valid @RequestBody PasswordResetRequest request,
                                      @RequestHeader(name = "idempotency-key")UUID idempotencyKey) {
        passwordResetService.resetPassword(request);
        return HttpResponse.builder()
                .status(OK.getReasonPhrase().toLowerCase())
                .message(PASSWORD_RESET)
                .statusCode(OK.value())
                .build();
    }


}
