package com.abranlezama.ecommercerestfulapi.authentication.controller;

import com.abranlezama.ecommercerestfulapi.authentication.dto.PasswordResetRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.service.PasswordResetService;
import com.abranlezama.ecommercerestfulapi.response.HttpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.abranlezama.ecommercerestfulapi.response.ResponseMessage.PASSWORD_RESET_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/auth/reset-password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping
    public HttpResponse requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        passwordResetService.requestPasswordReset(request.email());
        return HttpResponse.builder()
                .message(PASSWORD_RESET_REQUEST)
                .status(OK.getReasonPhrase().toLowerCase())
                .statusCode(OK.value())
                .build();
    }


}
