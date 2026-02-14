package com.epam.gym.core.util;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UsernameGeneratorTest {

    private UsernameGeneratorImpl usernameGenerator;
    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;

    @BeforeEach
    void setUp() {
        traineeStorage = new HashMap<>();
        trainerStorage = new HashMap<>();
        usernameGenerator = new UsernameGeneratorImpl(traineeStorage, trainerStorage);
    }

    @Test
    void generateUsername_CreateBasicUsername() {
        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith", username);
    }

    @Test
    void generateUsername_HandleDuplicateInTraineeStorage() {
        // create trainee with existing username
        Trainee existingTrainee = new Trainee();
        existingTrainee.setId(1L);
        existingTrainee.setFirstName("John");
        existingTrainee.setLastName("Smith");
        existingTrainee.setUsername("John.Smith");
        traineeStorage.put(1L, existingTrainee);

        // generate username for new user with same name
        String username = usernameGenerator.generateUsername("John", "Smith");

        assertEquals("John.Smith1", username);
    }

    @Test
    void generateUsername_HandleDuplicateInTrainerStorage() {
        // create trainer with uexisting username
        Trainer existingTrainer = new Trainer();
        existingTrainer.setId(1L);
        existingTrainer.setFirstName("Jane");
        existingTrainer.setLastName("Doe");
        existingTrainer.setUsername("Jane.Doe");
        trainerStorage.put(1L, existingTrainer);

        // generate username for new user with same name
        String username = usernameGenerator.generateUsername("Jane", "Doe");

        assertEquals("Jane.Doe1", username);
    }

    @Test
    void generateUsername_HandleMultipleDuplicates() {
        // create existing users
        Trainee trainee1 = new Trainee();
        trainee1.setUsername("Alice.Johnson");
        traineeStorage.put(1L, trainee1);

        Trainer trainer1 = new Trainer();
        trainer1.setUsername("Alice.Johnson1");
        trainerStorage.put(2L, trainer1);

        // generate username for 3rd user with same name
        String username = usernameGenerator.generateUsername("Alice", "Johnson");

        assertEquals("Alice.Johnson2", username);
    }

    @Test
    void generateUsername_IncrementSerialNumberSequentially() {
        // 1st call: no existing users
        String username1 = usernameGenerator.generateUsername("Bob", "Wilson");
        assertEquals("Bob.Wilson", username1);

        // add 1st user to storage
        Trainee trainee1 = new Trainee();
        trainee1.setUsername(username1);
        traineeStorage.put(1L, trainee1);

        // 2nd call: one existing user
        String username2 = usernameGenerator.generateUsername("Bob", "Wilson");
        assertEquals("Bob.Wilson1", username2);

        // add 2nd user to storage
        Trainee trainee2 = new Trainee();
        trainee2.setUsername(username2);
        traineeStorage.put(2L, trainee2);

        // 3rd call: 2 existing users
        String username3 = usernameGenerator.generateUsername("Bob", "Wilson");
        assertEquals("Bob.Wilson2", username3);
    }

    @Test
    void generateUsername_CheckDifferentNames() {
        String username1 = usernameGenerator.generateUsername("Michael", "Brown");
        String username2 = usernameGenerator.generateUsername("Sarah", "Davis");

        assertEquals("Michael.Brown", username1);
        assertEquals("Sarah.Davis", username2);
        assertNotEquals(username1, username2);
    }
}
