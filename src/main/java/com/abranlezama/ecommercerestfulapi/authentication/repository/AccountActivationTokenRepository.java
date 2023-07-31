package com.abranlezama.ecommercerestfulapi.authentication.repository;

import com.abranlezama.ecommercerestfulapi.authentication.model.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationToken, Long> {

    Optional<AccountActivationToken> findByToken(UUID token);
}
