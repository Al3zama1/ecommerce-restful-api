package com.abranlezama.ecommercerestfulapi.authentication.repository;

import com.abranlezama.ecommercerestfulapi.authentication.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

}
