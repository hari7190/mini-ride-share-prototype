// Entity class for User
package com.zamorincorp.rideshare.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // This will store the BCrypt hash

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        RIDER, DRIVER, ADMIN
    }
}
