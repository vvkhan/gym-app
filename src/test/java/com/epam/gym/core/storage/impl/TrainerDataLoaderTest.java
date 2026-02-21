package com.epam.gym.core.storage.impl;

import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.storage.InitialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDataLoaderTest {

    private TrainerDataLoader loader;
    private Map<Long, Trainer> storage;

    @BeforeEach
    void setUp() {
        loader = new TrainerDataLoader();
        storage = new HashMap<>();
        loader.setStorage(storage);
    }

    @Test
    void load_PopulatesStorageWithTrainers() {
        TrainingType specialization = new TrainingType();
        specialization.setId(1L);
        specialization.setTrainingTypeName("Cardio");

        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setFirstName("Jane");
        trainer.setLastName("Smith");
        trainer.setUsername("Jane.Smith");
        trainer.setIsActive(true);
        trainer.setSpecialization(specialization);

        InitialData data = new InitialData();
        data.setTrainers(List.of(trainer));
        loader.setInitialData(data);

        loader.load();

        assertEquals(1, storage.size());
        assertEquals("Jane", storage.get(1L).getFirstName());
        assertEquals("Cardio", storage.get(1L).getSpecialization().getTrainingTypeName());
    }

    @Test
    void load_DoesNothingWhenTrainersListIsNull() {
        InitialData data = new InitialData();
        data.setTrainers(null);
        loader.setInitialData(data);

        loader.load();

        assertTrue(storage.isEmpty());
    }

    @Test
    void load_HandlesMultipleTrainers() {
        TrainingType specialization = new TrainingType(1L, "Cardio");

        Trainer t1 = new Trainer();
        t1.setId(1L);
        t1.setFirstName("Alice");
        t1.setLastName("Smith");
        t1.setUsername("Alice.Smith");
        t1.setIsActive(true);
        t1.setSpecialization(specialization);

        Trainer t2 = new Trainer();
        t2.setId(2L);
        t2.setFirstName("Bob");
        t2.setLastName("Jones");
        t2.setUsername("Bob.Jones");
        t2.setIsActive(true);
        t2.setSpecialization(specialization);

        InitialData data = new InitialData();
        data.setTrainers(List.of(t1, t2));
        loader.setInitialData(data);

        loader.load();

        assertEquals(2, storage.size());
        assertEquals("Alice", storage.get(1L).getFirstName());
        assertEquals("Bob", storage.get(2L).getFirstName());
    }

    @Test
    void load_ThrowsExceptionWhenTrainerIsInvalid() {
        Trainer invalid = new Trainer();
        invalid.setId(1L);
        invalid.setFirstName("Jane");
        invalid.setLastName("Smith");
        invalid.setUsername("Jane.Smith");
        invalid.setIsActive(true);
        // specialization is missing

        InitialData data = new InitialData();
        data.setTrainers(List.of(invalid));
        loader.setInitialData(data);

        assertThrows(IllegalStateException.class, () -> loader.load());
    }
}
