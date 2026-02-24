package com.epam.gym.core.storage.impl;

import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.storage.InitialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDataLoaderTest {

    private TrainingDataLoader loader;
    private Map<Long, Training> storage;

    @BeforeEach
    void setUp() {
        loader = new TrainingDataLoader();
        storage = new HashMap<>();
        loader.setStorage(storage);
    }

    @Test
    void load_PopulatesStorageWithTrainings() {
        TrainingType type = new TrainingType();
        type.setId(1L);
        type.setTrainingTypeName("Cardio");

        Training training = new Training();
        training.setId(1L);
        training.setTraineeId(1L);
        training.setTrainerId(1L);
        training.setTrainingName("Morning Session");
        training.setTrainingType(type);
        training.setTrainingDate(LocalDate.of(2024, 1, 15));
        training.setDuration(60);

        InitialData data = new InitialData();
        data.setTrainings(List.of(training));
        loader.setInitialData(data);

        loader.load();

        assertEquals(1, storage.size());
        assertEquals("Morning Session", storage.get(1L).getTrainingName());
        assertEquals(60, storage.get(1L).getDuration());
    }

    @Test
    void load_DoesNothingWhenTrainingsListIsNull() {
        InitialData data = new InitialData();
        data.setTrainings(null);
        loader.setInitialData(data);

        loader.load();

        assertTrue(storage.isEmpty());
    }

    @Test
    void load_HandlesMultipleTrainings() {
        TrainingType type = new TrainingType(1L, "Cardio");

        Training t1 = new Training();
        t1.setId(1L);
        t1.setTraineeId(1L);
        t1.setTrainerId(1L);
        t1.setTrainingName("Session A");
        t1.setTrainingType(type);
        t1.setTrainingDate(LocalDate.of(2024, 1, 10));
        t1.setDuration(45);

        Training t2 = new Training();
        t2.setId(2L);
        t2.setTraineeId(2L);
        t2.setTrainerId(1L);
        t2.setTrainingName("Session B");
        t2.setTrainingType(type);
        t2.setTrainingDate(LocalDate.of(2024, 1, 12));
        t2.setDuration(60);

        InitialData data = new InitialData();
        data.setTrainings(List.of(t1, t2));
        loader.setInitialData(data);

        loader.load();

        assertEquals(2, storage.size());
        assertEquals("Session A", storage.get(1L).getTrainingName());
        assertEquals("Session B", storage.get(2L).getTrainingName());
    }

    @Test
    void load_ThrowsExceptionWhenTrainingIsInvalid() {
        Training invalid = new Training();
        invalid.setId(1L);
        invalid.setTraineeId(1L);
        invalid.setTrainerId(1L);
        invalid.setTrainingName("Session");
        invalid.setTrainingType(new TrainingType(1L, "Cardio"));
        // trainingDate and duration are missing

        InitialData data = new InitialData();
        data.setTrainings(List.of(invalid));
        loader.setInitialData(data);

        assertThrows(IllegalStateException.class, () -> loader.load());
    }
}
