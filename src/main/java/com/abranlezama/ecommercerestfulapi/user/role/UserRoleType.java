package com.abranlezama.ecommercerestfulapi.user.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.abranlezama.ecommercerestfulapi.user.role.UserRolePermission.READ_CUSTOMER;

@RequiredArgsConstructor
@Getter
public enum UserRoleType {
    CUSTOMER(
            Set.of(
                    READ_CUSTOMER
            )
    );

    private final Set<UserRolePermission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = this.permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
