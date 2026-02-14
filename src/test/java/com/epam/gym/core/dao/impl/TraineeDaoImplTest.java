package com.epam.gym.core.dao.impl;

import com.epam.gym.core.exception.EntityNotFoundException;
import com.epam.gym.core.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDaoImplTest {

    private TraineeDaoImpl traineeDao;
    private Map<Long, Trainee> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        traineeDao = new TraineeDaoImpl(storage);
    }

    @Test
    void create_GenerateIdAndStoreTrainee() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");

        Trainee created = traineeDao.create(trainee);

        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
        assertTrue(storage.containsKey(1L));
        assertEquals("John", storage.get(1L).getFirstName());
    }

    @Test
    void create_IncrementIdForMultipleTrainees() {
        Trainee trainee1 = new Trainee();
        trainee1.setFirstName("John");

        Trainee trainee2 = new Trainee();
        trainee2.setFirstName("Jane");

        Trainee created1 = traineeDao.create(trainee1);
        Trainee created2 = traineeDao.create(trainee2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
        assertEquals(2, storage.size());
    }

    @Test
    void findById_ReturnTraineeWhenExists() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("John");
        storage.put(1L, trainee);

        Optional<Trainee> found = traineeDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void findById_ReturnEmptyWhenNotExists() {
        Optional<Trainee> found = traineeDao.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void findByUsername_ReturnTraineeWhenExists() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUsername("John.Doe");
        storage.put(1L, trainee);

        Optional<Trainee> found = traineeDao.findByUsername("John.Doe");

        assertTrue(found.isPresent());
        assertEquals("John.Doe", found.get().getUsername());
    }

    @Test
    void findByUsername_ReturnEmptyWhenNotExists() {
        Optional<Trainee> found = traineeDao.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_ReturnAllTrainees() {
        Trainee trainee1 = new Trainee();
        trainee1.setId(1L);
        trainee1.setFirstName("John");

        Trainee trainee2 = new Trainee();
        trainee2.setId(2L);
        trainee2.setFirstName("Jane");

        storage.put(1L, trainee1);
        storage.put(2L, trainee2);

        List<Trainee> all = traineeDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAll_ReturnEmptyListWhenNoTrainees() {
        List<Trainee> all = traineeDao.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    void update_UpdateExistingTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("John");
        storage.put(1L, trainee);

        trainee.setFirstName("Jane");
        Trainee updated = traineeDao.update(trainee);

        assertEquals("Jane", updated.getFirstName());
        assertEquals("Jane", storage.get(1L).getFirstName());
    }

    @Test
    void update_ThrowExceptionWhenTraineeNotFound() {
        Trainee trainee = new Trainee();
        trainee.setId(999L);
        trainee.setFirstName("John");

        assertThrows(EntityNotFoundException.class, () -> traineeDao.update(trainee));
    }

    @Test
    void delete_RemoveTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        storage.put(1L, trainee);

        traineeDao.delete(1L);

        assertFalse(storage.containsKey(1L));
        assertTrue(storage.isEmpty());
    }

    @Test
    void delete_ThrowExceptionWhenTraineeNotFound() {
        assertThrows(EntityNotFoundException.class, () -> traineeDao.delete(999L));
    }
}
