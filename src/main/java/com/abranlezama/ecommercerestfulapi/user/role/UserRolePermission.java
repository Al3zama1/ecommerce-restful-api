package com.abranlezama.ecommercerestfulapi.user.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserRolePermission {
    READ_CUSTOMER("READ:CUSTOMER");

    private final String permission;
}
