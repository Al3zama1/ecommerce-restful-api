package com.abranlezama.ecommercerestfulapi.authentication.service;

import com.abranlezama.ecommercerestfulapi.authentication.dto.AuthenticationRequest;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegistrationRequest;

import java.util.Map;

public interface AuthenticationService {

    long registerCustomer(RegistrationRequest request);
    Map<String, String> authenticateCustomer(AuthenticationRequest request);
}
