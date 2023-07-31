package com.abranlezama.ecommercerestfulapi.jwt.service.imp;

import com.abranlezama.ecommercerestfulapi.jwt.service.JwtService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
public class JwtServiceImp implements JwtService {

    private static final String JWT_ISSUER = "Abran Lezama";
    private static final String JWT_AUDIENCE = "application users";
    private static final String AUTHORITIES = "authorities";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1_800_000; // 30 minutes

    private final Clock clock;

    @Value("${custom.jwt.secret}")
    private String jwtSecret;

    @Override
    public String createAccessToken(UserDetails userDetails) {
        String[] claims = getClaimsFromUser(userDetails);
        return JWT.create().withIssuer(JWT_ISSUER).withAudience(JWT_AUDIENCE)
                .withIssuedAt(Instant.now(clock)).withSubject(userDetails.getUsername())
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(Instant.now(clock).plusSeconds(ACCESS_TOKEN_EXPIRATION_TIME))
                .sign(HMAC512(jwtSecret.getBytes()));
    }

    @Override
    public String createRefreshToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getSubject(String token) {
        return getJWTVerifier().verify(token).getSubject();
    }

    @Override
    public List<GrantedAuthority> getAuthorities(String token) {
        String[] claims = getClaimsFromToken(token);
        return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public boolean isTokenValid(String email, String token) {
        JWTVerifier verifier = getJWTVerifier();
        return StringUtils.isNotEmpty(email) && !isTokenExpired(verifier, token);
    }

    private String[] getClaimsFromUser(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        Algorithm algorithm = HMAC512(jwtSecret);
        return JWT.require(algorithm).withIssuer(JWT_ISSUER).build();
    }
}
