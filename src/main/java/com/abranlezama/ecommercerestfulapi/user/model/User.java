package com.abranlezama.ecommercerestfulapi.user.model;

import com.abranlezama.ecommercerestfulapi.user.role.UserRoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    public User(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.phone = user.getPhone();
        this.imageUrl = user.getImageUrl();
        this.usingMfa = user.getUsingMfa();
        this.role = user.getRole();
        this.enabled = user.getEnabled();
        this.nonLocked = user.getNonLocked();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false)
    private String firstName;
    @Column(length = 50, nullable = false)
    private String lastName;
    @Column(length = 50, nullable = false, updatable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRoleType role;
    @Column(nullable = false)
    @Builder.Default
    private String imageUrl = "https://cdn-icons-png.flaticon.com/512/149/149071.png";
    @Column(length = 10)
    private String phone;
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;
    @Column(nullable = false)
    @Builder.Default
    private Boolean nonLocked = true;
    @Column(nullable = false)
    @Builder.Default
    private Boolean usingMfa = false;
}
