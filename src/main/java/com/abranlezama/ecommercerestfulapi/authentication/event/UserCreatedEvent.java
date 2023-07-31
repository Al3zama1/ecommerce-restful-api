package com.abranlezama.ecommercerestfulapi.authentication.event;

import com.abranlezama.ecommercerestfulapi.user.model.User;

public record UserCreatedEvent(
        User user
) { }
