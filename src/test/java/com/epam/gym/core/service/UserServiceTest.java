package com.epam.gym.core.service;

import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void changePassword_UpdatesPasswordSuccessfully() {
        User user = User.builder().username("alice").password("$2a$encoded").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "$2a$encoded")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("$2a$newEncoded");

        userService.changePassword("alice", "oldPass", "newPass");

        assertEquals("$2a$newEncoded", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ThrowWhenOldPasswordIsWrong() {
        User user = User.builder().username("alice").password("$2a$encoded").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "$2a$encoded")).thenReturn(false);

        assertThrows(SecurityException.class, () ->
                userService.changePassword("alice", "wrongPass", "newPass"));
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePassword_ThrowWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                userService.changePassword("unknown", "old", "new"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void recordLogout_SetsLastLogoutTimestamp() {
        User user = User.builder().username("alice").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        LocalDateTime before = LocalDateTime.now();
        userService.recordLogout("alice");
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(user.getLastLogout());
        assertFalse(user.getLastLogout().isBefore(before));
        assertFalse(user.getLastLogout().isAfter(after));
        verify(userRepository).save(user);
    }

    @Test
    void recordLogout_NoOpWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userService.recordLogout("ghost"));
        verify(userRepository, never()).save(any());
    }
}
