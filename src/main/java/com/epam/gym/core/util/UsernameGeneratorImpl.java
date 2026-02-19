package com.epam.gym.core.util;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Stream;

@Component
public class UsernameGeneratorImpl implements UsernameGenerator {

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
        String baseUsername = firstName + "." + lastName;

        if (!usernameExists(baseUsername)) {
            return baseUsername;
        }

        // base username exists, find next available with serial number
        // infinite for (empty condition)
        // NOTE: remember abt N+1 problem for JPA!
        for (int counter = 1; ; counter++) {
            String username = baseUsername + counter;
            if (!usernameExists(username)) {
                return username;
            }
        }
    }

    private boolean usernameExists(String username) {
        return Stream.concat(traineeStorage.values().stream(), trainerStorage.values().stream())
                .anyMatch(user -> username.equals(user.getUsername()));
    }
}
