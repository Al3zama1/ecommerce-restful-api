package com.abranlezama.ecommercerestfulapi.authentication.repository;

import com.abranlezama.ecommercerestfulapi.authentication.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(UUID token);
}
