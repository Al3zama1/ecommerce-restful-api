package com.abranlezama.ecommercerestfulapi.authentication.model;

import com.abranlezama.ecommercerestfulapi.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "account_activation_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private UUID token;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false, unique = true)
    private User user;
}
