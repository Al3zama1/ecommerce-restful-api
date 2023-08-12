package com.abranlezama.ecommercerestfulapi.authentication.service;

import com.abranlezama.ecommercerestfulapi.authentication.dto.AuthenticationRequest;
import com.abranlezama.ecommercerestfulapi.authentication.dto.RegistrationRequest;
import com.abranlezama.ecommercerestfulapi.user.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {

    long registerCustomer(RegistrationRequest request);
    UserDetails authenticateCustomer(AuthenticationRequest request);

    String createRefreshToken(User user);
}
