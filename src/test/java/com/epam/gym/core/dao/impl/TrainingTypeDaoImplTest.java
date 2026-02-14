package com.epam.gym.core.dao.impl;

import com.epam.gym.core.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTypeDaoImplTest {

    private TrainingTypeDaoImpl trainingTypeDao;
    private Map<Long, TrainingType> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainingTypeDao = new TrainingTypeDaoImpl(storage);
    }

    @Test
    void create_GenerateIdAndStoreTrainingType() {
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Cardio");

        TrainingType created = trainingTypeDao.create(trainingType);

        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
        assertTrue(storage.containsKey(1L));
        assertEquals("Cardio", storage.get(1L).getTrainingTypeName());
    }

    @Test
    void create_IncrementIdForMultipleTrainingTypes() {
        TrainingType type1 = new TrainingType();
        type1.setTrainingTypeName("Cardio");

        TrainingType type2 = new TrainingType();
        type2.setTrainingTypeName("Strength");

        TrainingType created1 = trainingTypeDao.create(type1);
        TrainingType created2 = trainingTypeDao.create(type2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
        assertEquals(2, storage.size());
    }

    @Test
    void findById_ReturnTrainingTypeWhenExists() {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Cardio");
        storage.put(1L, trainingType);

        Optional<TrainingType> found = trainingTypeDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Cardio", found.get().getTrainingTypeName());
    }

    @Test
    void findById_ReturnEmptyWhenNotExists() {
        Optional<TrainingType> found = trainingTypeDao.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void findByName_ReturnTrainingTypeWhenExists() {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Cardio");
        storage.put(1L, trainingType);

        Optional<TrainingType> found = trainingTypeDao.findByName("Cardio");

        assertTrue(found.isPresent());
        assertEquals("Cardio", found.get().getTrainingTypeName());
    }

    @Test
    void findByName_ReturnEmptyWhenNotExists() {
        Optional<TrainingType> found = trainingTypeDao.findByName("NonExistent");

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_ReturnAllTrainingTypes() {
        TrainingType type1 = new TrainingType();
        type1.setId(1L);
        type1.setTrainingTypeName("Cardio");

        TrainingType type2 = new TrainingType();
        type2.setId(2L);
        type2.setTrainingTypeName("Strength");

        storage.put(1L, type1);
        storage.put(2L, type2);

        List<TrainingType> all = trainingTypeDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAll_ReturnEmptyListWhenNoTrainingTypes() {
        List<TrainingType> all = trainingTypeDao.findAll();

        assertTrue(all.isEmpty());
    }
}
