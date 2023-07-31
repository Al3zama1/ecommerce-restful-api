package com.abranlezama.ecommercerestfulapi.authentication.service;

import com.abranlezama.ecommercerestfulapi.user.model.User;

public interface AccountActivationService {

    void createAccountActivationToken(User user);
    void activateCustomerAccount(String token);
}
