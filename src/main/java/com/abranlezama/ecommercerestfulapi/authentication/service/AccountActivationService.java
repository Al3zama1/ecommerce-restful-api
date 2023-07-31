package com.abranlezama.ecommercerestfulapi.authentication.service;

import com.abranlezama.ecommercerestfulapi.authentication.event.UserCreatedEvent;

public interface AccountActivationService {

    void createAccountActivationToken(UserCreatedEvent event);
    void activateCustomerAccount(String token);
}
