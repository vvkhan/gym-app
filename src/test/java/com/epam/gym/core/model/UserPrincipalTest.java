package com.epam.gym.core.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    @Test
    void isAccountNonLocked_ReturnTrueWhenNoLockSet() {
        UserPrincipal principal = principalWithLock(null);

        assertTrue(principal.isAccountNonLocked());
    }

    @Test
    void isAccountNonLocked_ReturnFalseWhenLockIsInFuture() {
        UserPrincipal principal = principalWithLock(LocalDateTime.now().plusMinutes(5));

        assertFalse(principal.isAccountNonLocked());
    }

    @Test
    void isAccountNonLocked_ReturnTrueWhenLockHasExpired() {
        UserPrincipal principal = principalWithLock(LocalDateTime.now().minusSeconds(1));

        assertTrue(principal.isAccountNonLocked());
    }

    private UserPrincipal principalWithLock(LocalDateTime lockedUntil) {
        User user = User.builder().username("alice").password("pass").isActive(true).build();
        user.setAccountLockedUntil(lockedUntil);
        return new UserPrincipal(user, List.of(new SimpleGrantedAuthority("ROLE_TRAINEE")));
    }
}
