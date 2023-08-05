package com.abranlezama.ecommercerestfulapi.authentication.repository;

import com.abranlezama.ecommercerestfulapi.authentication.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(UUID token);
}
