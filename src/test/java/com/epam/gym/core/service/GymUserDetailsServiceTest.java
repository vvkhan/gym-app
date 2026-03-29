package com.epam.gym.core.service;

import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.User;
import com.epam.gym.core.model.UserPrincipal;
import com.epam.gym.core.repository.TrainerRepository;
import com.epam.gym.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private GymUserDetailsService gymUserDetailsService;

    @Test
    void loadUserByUsername_AssignsTrainerRole() {
        User user = User.builder().username("trainer1").password("pass").isActive(true).build();
        when(userRepository.findByUsername("trainer1")).thenReturn(Optional.of(user));
        when(trainerRepository.findByUserUsername("trainer1")).thenReturn(Optional.of(new Trainer()));

        UserDetails result = gymUserDetailsService.loadUserByUsername("trainer1");

        assertInstanceOf(UserPrincipal.class, result);
        assertEquals("trainer1", result.getUsername());
        assertEquals("ROLE_TRAINER",
                result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_AssignsTraineeRoleWhenNotATrainer() {
        User user = User.builder().username("trainee1").password("pass").isActive(true).build();
        when(userRepository.findByUsername("trainee1")).thenReturn(Optional.of(user));
        when(trainerRepository.findByUserUsername("trainee1")).thenReturn(Optional.empty());

        UserDetails result = gymUserDetailsService.loadUserByUsername("trainee1");

        assertEquals("ROLE_TRAINEE",
                result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_ThrowWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                gymUserDetailsService.loadUserByUsername("ghost"));
    }
}
