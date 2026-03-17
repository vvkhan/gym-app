package com.epam.gym.core.service;

import com.epam.gym.core.aspect.LogExecution;
import com.epam.gym.core.model.User;
import com.epam.gym.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@LogExecution
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String username, String password) {
        return userRepository.existsByUsernameAndPassword(username, password);
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        user.setPassword(newPassword);
        userRepository.save(user);
    }
}
