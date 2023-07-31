package com.abranlezama.ecommercerestfulapi.authentication.controller;

import com.abranlezama.ecommercerestfulapi.authentication.service.AuthenticationService;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegisterRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerCustomer(@Valid @RequestBody RegisterRequestDTO request,
                                                 UriComponentsBuilder uriComponentsBuilder) {
        long userId = authenticationService.registerCustomer(request);
        UriComponents uriComponents = uriComponentsBuilder.path("/api/v1/users/{id}").buildAndExpand(userId);
        return ResponseEntity.created(URI.create(uriComponents.toUriString())).build();
    }
}
