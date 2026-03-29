package com.epam.gym.core.service;

import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    @Test
    void recordFailure_IncrementsAttemptCounter() {
        User user = userWithAttempts(1);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        loginAttemptService.recordFailure("alice");

        assertEquals(2, user.getFailedLoginAttempts());
        assertNotNull(user.getLastFailedLoginAt());
        assertNull(user.getAccountLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void recordFailure_LocksAccountWhenThresholdReached() {
        User user = userWithAttempts(2);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        LocalDateTime before = LocalDateTime.now();
        loginAttemptService.recordFailure("alice");
        LocalDateTime after = LocalDateTime.now();

        assertEquals(3, user.getFailedLoginAttempts());
        assertNotNull(user.getAccountLockedUntil());
        assertTrue(user.getAccountLockedUntil().isAfter(before.plusMinutes(4)));
        assertTrue(user.getAccountLockedUntil().isBefore(after.plusMinutes(6)));
        verify(userRepository).save(user);
    }

    @Test
    void recordFailure_NoOpWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> loginAttemptService.recordFailure("ghost"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void recordSuccess_ResetsAllSecurityFields() {
        User user = userWithAttempts(3);
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(5));
        user.setLastFailedLoginAt(LocalDateTime.now());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        loginAttemptService.recordSuccess("alice");

        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getAccountLockedUntil());
        assertNull(user.getLastFailedLoginAt());
        verify(userRepository).save(user);
    }

    @Test
    void recordSuccess_NoOpWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> loginAttemptService.recordSuccess("ghost"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void isLocked_ReturnTrueWhenLockIsInFuture() {
        User user = userWithAttempts(3);
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertTrue(loginAttemptService.isLocked("alice"));
    }

    @Test
    void isLocked_ReturnFalseWhenLockHasExpired() {
        User user = userWithAttempts(3);
        user.setAccountLockedUntil(LocalDateTime.now().minusSeconds(1));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertFalse(loginAttemptService.isLocked("alice"));
    }

    @Test
    void isLocked_ReturnFalseWhenNotLocked() {
        User user = userWithAttempts(1);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertFalse(loginAttemptService.isLocked("alice"));
    }

    @Test
    void isLocked_ReturnFalseWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertFalse(loginAttemptService.isLocked("ghost"));
    }

    private User userWithAttempts(int attempts) {
        User user = User.builder().username("alice").password("pass").isActive(true).build();
        user.setFailedLoginAttempts(attempts);
        return user;
    }
}
