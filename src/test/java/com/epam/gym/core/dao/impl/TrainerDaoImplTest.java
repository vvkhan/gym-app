package com.epam.gym.core.dao.impl;

import com.epam.gym.core.exception.EntityNotFoundException;
import com.epam.gym.core.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoImplTest {

    private TrainerDaoImpl trainerDao;
    private Map<Long, Trainer> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainerDao = new TrainerDaoImpl(storage);
    }

    @Test
    void create_GenerateIdAndStoreTrainer() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Doe");

        Trainer created = trainerDao.create(trainer);

        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
        assertTrue(storage.containsKey(1L));
        assertEquals("John", storage.get(1L).getFirstName());
    }

    @Test
    void create_IncrementIdForMultipleTrainers() {
        Trainer trainer1 = new Trainer();
        trainer1.setFirstName("John");

        Trainer trainer2 = new Trainer();
        trainer2.setFirstName("Jane");

        Trainer created1 = trainerDao.create(trainer1);
        Trainer created2 = trainerDao.create(trainer2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
        assertEquals(2, storage.size());
    }

    @Test
    void findById_ReturnTrainerWhenExists() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setFirstName("John");
        storage.put(1L, trainer);

        Optional<Trainer> found = trainerDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void findById_ReturnEmptyWhenNotExists() {
        Optional<Trainer> found = trainerDao.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void findByUsername_ReturnTrainerWhenExists() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUsername("John.Doe");
        storage.put(1L, trainer);

        Optional<Trainer> found = trainerDao.findByUsername("John.Doe");

        assertTrue(found.isPresent());
        assertEquals("John.Doe", found.get().getUsername());
    }

    @Test
    void findByUsername_ReturnEmptyWhenNotExists() {
        Optional<Trainer> found = trainerDao.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_ReturnAllTrainers() {
        Trainer trainer1 = new Trainer();
        trainer1.setId(1L);
        trainer1.setFirstName("John");

        Trainer trainer2 = new Trainer();
        trainer2.setId(2L);
        trainer2.setFirstName("Jane");

        storage.put(1L, trainer1);
        storage.put(2L, trainer2);

        List<Trainer> all = trainerDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAll_ReturnEmptyListWhenNoTrainers() {
        List<Trainer> all = trainerDao.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    void update_UpdateExistingTrainer() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setFirstName("John");
        storage.put(1L, trainer);

        trainer.setFirstName("Jane");
        Trainer updated = trainerDao.update(trainer);

        assertEquals("Jane", updated.getFirstName());
        assertEquals("Jane", storage.get(1L).getFirstName());
    }

    @Test
    void update_ThrowExceptionWhenTrainerNotFound() {
        Trainer trainer = new Trainer();
        trainer.setId(999L);
        trainer.setFirstName("John");

        assertThrows(EntityNotFoundException.class, () -> trainerDao.update(trainer));
    }
}
