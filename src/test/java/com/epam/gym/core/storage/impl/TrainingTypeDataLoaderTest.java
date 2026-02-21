package com.epam.gym.core.storage.impl;

import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.storage.InitialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTypeDataLoaderTest {

    private TrainingTypeDataLoader loader;
    private Map<Long, TrainingType> storage;

    @BeforeEach
    void setUp() {
        loader = new TrainingTypeDataLoader();
        storage = new HashMap<>();
        loader.setStorage(storage);
    }

    @Test
    void load_PopulatesStorageWithTrainingTypes() {
        TrainingType cardio = new TrainingType();
        cardio.setId(1L);
        cardio.setTrainingTypeName("Cardio");

        TrainingType strength = new TrainingType();
        strength.setId(2L);
        strength.setTrainingTypeName("Strength");

        InitialData data = new InitialData();
        data.setTrainingTypes(List.of(cardio, strength));
        loader.setInitialData(data);

        loader.load();

        assertEquals(2, storage.size());
        assertEquals("Cardio", storage.get(1L).getTrainingTypeName());
        assertEquals("Strength", storage.get(2L).getTrainingTypeName());
    }

    @Test
    void load_DoesNothingWhenTrainingTypesListIsNull() {
        InitialData data = new InitialData();
        data.setTrainingTypes(null);
        loader.setInitialData(data);

        loader.load();

        assertTrue(storage.isEmpty());
    }

    @Test
    void load_HandlesSingleTrainingType() {
        TrainingType type = new TrainingType();
        type.setId(1L);
        type.setTrainingTypeName("Yoga");

        InitialData data = new InitialData();
        data.setTrainingTypes(List.of(type));
        loader.setInitialData(data);

        loader.load();

        assertEquals(1, storage.size());
        assertEquals("Yoga", storage.get(1L).getTrainingTypeName());
    }
}
