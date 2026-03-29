package com.epam.gym.core.service;

import com.epam.gym.core.model.User;
import com.epam.gym.core.model.UserPrincipal;
import com.epam.gym.core.repository.TrainerRepository;
import com.epam.gym.core.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GymUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;

    public GymUserDetailsService(UserRepository userRepository, TrainerRepository trainerRepository) {
        this.userRepository = userRepository;
        this.trainerRepository = trainerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String role = trainerRepository.findByUserUsername(username).isPresent()
                ? "ROLE_TRAINER"
                : "ROLE_TRAINEE";

        return new UserPrincipal(user, List.of(new SimpleGrantedAuthority(role)));
    }
}
