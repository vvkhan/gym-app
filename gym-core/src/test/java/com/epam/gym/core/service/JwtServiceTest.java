package com.epam.gym.core.service;

import com.epam.gym.core.model.User;
import com.epam.gym.core.model.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // 32 zero-bytes encoded as Base64 — valid HMAC-SHA256 key for test use
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        jwtService = new JwtService(secret, 86400000L);
    }

    @Test
    void isTokenValid_ReturnTrueWhenNoLastLogout() {
        UserPrincipal principal = principalWithLogout("alice", null);
        String token = jwtService.generateToken(principal);

        assertTrue(jwtService.isTokenValid(token, principal));
    }

    @Test
    void isTokenValid_ReturnTrueWhenLastLogoutIsBeforeTokenIssuedAt() {
        // Token is issued "now"; last logout happened in the past → token is still valid
        UserPrincipal principal = principalWithLogout("alice", LocalDateTime.now().minusHours(1));
        String token = jwtService.generateToken(principal);

        assertTrue(jwtService.isTokenValid(token, principal));
    }

    @Test
    void isTokenValid_ReturnFalseWhenLastLogoutIsAfterTokenIssuedAt() {
        // Token is issued "now"; last logout is in the future → any existing token pre-dates it
        UserPrincipal principal = principalWithLogout("alice", LocalDateTime.now().plusSeconds(5));
        String token = jwtService.generateToken(principal);

        assertFalse(jwtService.isTokenValid(token, principal));
    }

    @Test
    void isTokenValid_ReturnFalseWhenUsernameDoesNotMatch() {
        UserPrincipal alice = principalWithLogout("alice", null);
        UserPrincipal bob = principalWithLogout("bob", null);
        String token = jwtService.generateToken(alice);

        assertFalse(jwtService.isTokenValid(token, bob));
    }

    private UserPrincipal principalWithLogout(String username, LocalDateTime lastLogout) {
        User user = User.builder().username(username).password("pass").isActive(true).build();
        user.setLastLogout(lastLogout);
        return new UserPrincipal(user, List.of(new SimpleGrantedAuthority("ROLE_TRAINEE")));
    }
}
