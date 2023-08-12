package com.abranlezama.ecommercerestfulapi.authentication.model;

import com.abranlezama.ecommercerestfulapi.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID token;
    private Instant expiresAt;
    private Instant createdAt;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false)
    private User user;
}
