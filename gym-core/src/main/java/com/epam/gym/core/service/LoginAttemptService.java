package com.epam.gym.core.service;

import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_MINUTES = 5;

    private final UserRepository userRepository;

    public LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void recordFailure(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            user.setLastFailedLoginAt(LocalDateTime.now());
            if (attempts >= MAX_ATTEMPTS) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            }
            userRepository.save(user);
        });
    }

    @Transactional
    public void recordSuccess(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            user.setLastFailedLoginAt(null);
            userRepository.save(user);
        });
    }

    public boolean isLocked(String username) {
        return userRepository.findByUsername(username)
                .map(User::getAccountLockedUntil)
                .map(lockedUntil -> LocalDateTime.now().isBefore(lockedUntil))
                .orElse(false);
    }
}
