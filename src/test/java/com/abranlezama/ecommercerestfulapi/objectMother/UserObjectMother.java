package com.abranlezama.ecommercerestfulapi.objectMother;


import com.abranlezama.ecommercerestfulapi.user.model.User;

import static com.abranlezama.ecommercerestfulapi.user.role.UserRoleType.CUSTOMER;

public class UserObjectMother {
    public static User.UserBuilder customer() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Last")
                .password("12345678")
                .email("john.last@gmail.com")
                .role(CUSTOMER);
    }
}
