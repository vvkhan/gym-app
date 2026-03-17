package com.epam.gym.core.repository;

import com.epam.gym.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsernameAndPassword(String username, String password);

    Optional<User> findByUsername(String username);
}
