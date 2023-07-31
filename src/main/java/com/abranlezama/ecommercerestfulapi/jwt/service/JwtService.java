package com.abranlezama.ecommercerestfulapi.jwt.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface JwtService {
    String createAccessToken(UserDetails userDetails);
    String createRefreshToken();
    String getSubject(String token);
    List<GrantedAuthority> getAuthorities(String token);
    boolean isTokenValid(String email, String token);
}
