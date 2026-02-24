package com.epam.gym.core.dao.impl;

import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoImplTest {

    private TrainingDaoImpl trainingDao;
    private Map<Long, Training> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainingDao = new TrainingDaoImpl();
        trainingDao.setStorage(storage);
    }

    @Test
    void create_GenerateIdAndStoreTraining() {
        Training training = new Training();
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setTrainingName("Cardio Session");

        Training created = trainingDao.create(training);

        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
        assertTrue(storage.containsKey(1L));
        assertEquals("Cardio Session", storage.get(1L).getTrainingName());
    }

    @Test
    void create_IncrementIdForMultipleTrainings() {
        Training training1 = new Training();
        training1.setTrainingName("Session 1");

        Training training2 = new Training();
        training2.setTrainingName("Session 2");

        Training created1 = trainingDao.create(training1);
        Training created2 = trainingDao.create(training2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
        assertEquals(2, storage.size());
    }

    @Test
    void findById_ReturnTrainingWhenExists() {
        Training training = new Training();
        training.setId(1L);
        training.setTrainingName("Cardio");
        storage.put(1L, training);

        Optional<Training> found = trainingDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Cardio", found.get().getTrainingName());
    }

    @Test
    void findById_ReturnEmptyWhenNotExists() {
        Optional<Training> found = trainingDao.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void findById_ThrowExceptionWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.findById(null));
    }

    @Test
    void findAll_ReturnAllTrainings() {
        Training training1 = new Training();
        training1.setId(1L);
        training1.setTrainingName("Session 1");

        Training training2 = new Training();
        training2.setId(2L);
        training2.setTrainingName("Session 2");

        storage.put(1L, training1);
        storage.put(2L, training2);

        List<Training> all = trainingDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAll_ReturnEmptyListWhenNoTrainings() {
        List<Training> all = trainingDao.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    void findByTraineeId_ReturnTrainingsForTrainee() {
        Training training1 = new Training();
        training1.setId(1L);
        training1.setTraineeId(100L);
        training1.setTrainingName("Session 1");

        Training training2 = new Training();
        training2.setId(2L);
        training2.setTraineeId(100L);
        training2.setTrainingName("Session 2");

        Training training3 = new Training();
        training3.setId(3L);
        training3.setTraineeId(200L);
        training3.setTrainingName("Session 3");

        storage.put(1L, training1);
        storage.put(2L, training2);
        storage.put(3L, training3);

        List<Training> traineeTrainings = trainingDao.findByTraineeId(100L);

        assertEquals(2, traineeTrainings.size());
        assertTrue(traineeTrainings.stream().allMatch(t -> t.getTraineeId().equals(100L)));
    }

    @Test
    void findByTraineeId_ReturnEmptyListWhenNoMatch() {
        List<Training> traineeTrainings = trainingDao.findByTraineeId(999L);

        assertTrue(traineeTrainings.isEmpty());
    }

    @Test
    void findByTraineeId_ThrowExceptionWhenTraineeIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.findByTraineeId(null));
    }

    @Test
    void findByTrainerId_ReturnTrainingsForTrainer() {
        Training training1 = new Training();
        training1.setId(1L);
        training1.setTrainerId(50L);
        training1.setTrainingName("Session 1");

        Training training2 = new Training();
        training2.setId(2L);
        training2.setTrainerId(50L);
        training2.setTrainingName("Session 2");

        Training training3 = new Training();
        training3.setId(3L);
        training3.setTrainerId(60L);
        training3.setTrainingName("Session 3");

        storage.put(1L, training1);
        storage.put(2L, training2);
        storage.put(3L, training3);

        List<Training> trainerTrainings = trainingDao.findByTrainerId(50L);

        assertEquals(2, trainerTrainings.size());
        assertTrue(trainerTrainings.stream().allMatch(t -> t.getTrainerId().equals(50L)));
    }

    @Test
    void findByTrainerId_ReturnEmptyListWhenNoMatch() {
        List<Training> trainerTrainings = trainingDao.findByTrainerId(999L);

        assertTrue(trainerTrainings.isEmpty());
    }

    @Test
    void findByTrainerId_ThrowExceptionWhenTrainerIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.findByTrainerId(null));
    }
}
