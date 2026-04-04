package com.epam.gym.core.service;

import com.epam.gym.core.aspect.LogExecution;
import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@LogExecution
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastLogout(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void recordLogout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogout(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
