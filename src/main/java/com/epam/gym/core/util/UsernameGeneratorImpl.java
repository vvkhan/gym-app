package com.epam.gym.core.util;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UsernameGeneratorImpl implements UsernameGenerator {

    private static final Logger log = LoggerFactory.getLogger(UsernameGeneratorImpl.class);

    private final Map<Long, Trainee> traineeStorage;
    private final Map<Long, Trainer> trainerStorage;

    @Autowired
    public UsernameGeneratorImpl(
            @Qualifier("traineeStorage") Map<Long, Trainee> traineeStorage,
            @Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage) {
        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
    }

    @Override
    public String generateUsername(String firstName, String lastName) {
        log.debug("Generating username for: {} {}", firstName, lastName);

        String baseUsername = firstName + "." + lastName;

        if (!usernameExists(baseUsername)) {
            log.debug("Username '{}' is available", baseUsername);
            return baseUsername;
        }

        // base username exists, find next available with serial number
        int counter = 1;
        while (true) {
            String username = baseUsername + counter;
            if (!usernameExists(username)) {
                log.info("Username '{}' already exists. Generated unique username: '{}'", baseUsername, username);
                return username;
            }
            counter++;
        }
    }

    private boolean usernameExists(String username) {
        boolean existsInTrainees = traineeStorage.values().stream()
                .anyMatch(trainee -> username.equals(trainee.getUsername()));

        if (existsInTrainees) {
            return true;
        }

        return trainerStorage.values().stream()
                .anyMatch(trainer -> username.equals(trainer.getUsername()));
    }
}
