package com.abranlezama.ecommercerestfulapi.authentication.service;

import com.abranlezama.ecommercerestfulapi.authentication.dto.RegisterRequestDTO;

public interface AuthenticationService {

    long registerCustomer(RegisterRequestDTO request);
}
