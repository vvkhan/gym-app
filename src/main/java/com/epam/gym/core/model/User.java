package com.epam.gym.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Builder;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    private UUID id;

    @Column(name = "first_name", nullable = false)
    @ToString.Include
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @ToString.Include
    private String lastName;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String username;

    @Column(nullable = false)
    private String password;

    @Transient
    private String rawPassword;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;

    @Column(name = "last_logout")
    private LocalDateTime lastLogout;

}
