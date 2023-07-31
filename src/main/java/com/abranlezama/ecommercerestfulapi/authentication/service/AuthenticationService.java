package com.abranlezama.ecommercerestfulapi.authentication.service;

import com.abranlezama.ecommercerestfulapi.authentication.dto.LoginRequestDTO;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegisterRequestDTO;

import java.util.Map;

public interface AuthenticationService {

    long registerCustomer(RegisterRequestDTO request);
    Map<String, String> authenticateCustomer(LoginRequestDTO request);
}
