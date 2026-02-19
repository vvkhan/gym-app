package com.epam.gym.core.storage.impl;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.storage.InitialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDataLoaderTest {

    private TraineeDataLoader loader;
    private Map<Long, Trainee> storage;

    @BeforeEach
    void setUp() {
        loader = new TraineeDataLoader();
        storage = new HashMap<>();
        loader.setStorage(storage);
    }

    @Test
    void load_PopulatesStorageWithTrainees() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername("John.Doe");
        trainee.setIsActive(true);

        InitialData data = new InitialData();
        data.setTrainees(List.of(trainee));
        loader.setInitialData(data);

        loader.load();

        assertEquals(1, storage.size());
        assertEquals("John", storage.get(1L).getFirstName());
        assertEquals("John.Doe", storage.get(1L).getUsername());
    }

    @Test
    void load_DoesNothingWhenTraineesListIsNull() {
        InitialData data = new InitialData();
        data.setTrainees(null);
        loader.setInitialData(data);

        loader.load();

        assertTrue(storage.isEmpty());
    }

    @Test
    void load_HandlesMultipleTrainees() {
        Trainee t1 = new Trainee();
        t1.setId(1L);
        t1.setFirstName("Alice");
        t1.setLastName("Smith");
        t1.setUsername("Alice.Smith");
        t1.setIsActive(true);

        Trainee t2 = new Trainee();
        t2.setId(2L);
        t2.setFirstName("Bob");
        t2.setLastName("Jones");
        t2.setUsername("Bob.Jones");
        t2.setIsActive(false);

        InitialData data = new InitialData();
        data.setTrainees(List.of(t1, t2));
        loader.setInitialData(data);

        loader.load();

        assertEquals(2, storage.size());
        assertEquals("Alice", storage.get(1L).getFirstName());
        assertEquals("Bob", storage.get(2L).getFirstName());
    }

    @Test
    void load_ThrowsExceptionWhenTraineeIsInvalid() {
        Trainee invalid = new Trainee();
        invalid.setId(1L);
        invalid.setFirstName("John");
        invalid.setLastName("Doe");
        // username is missing

        InitialData data = new InitialData();
        data.setTrainees(List.of(invalid));
        loader.setInitialData(data);

        assertThrows(IllegalStateException.class, () -> loader.load());
    }
}
