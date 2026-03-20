package com.epam.gym.core.service;

import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void authenticate_ReturnTrueWhenCredentialsMatch() {
        when(userRepository.existsByUsernameAndPassword("alice", "pass")).thenReturn(true);

        assertTrue(userService.authenticate("alice", "pass"));
    }

    @Test
    void changePassword_UpdatesPasswordSuccessfully() {
        User user = User.builder().username("alice").password("oldPass").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        userService.changePassword("alice", "newPass");

        assertEquals("newPass", user.getPassword());
        verify(userRepository).save(user);
    }
}
