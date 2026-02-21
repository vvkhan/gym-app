package com.epam.gym.core.service;

import com.epam.gym.core.dao.TrainerDao;
import com.epam.gym.core.dao.TrainingTypeDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.util.PasswordGenerator;
import com.epam.gym.core.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void createTrainer_GenerateCredentialsAndCreateTrainer() {
        String firstName = "John";
        String lastName = "Doe";
        Long trainingTypeId = 1L;

        TrainingType trainingType = new TrainingType();
        trainingType.setId(trainingTypeId);
        trainingType.setTrainingTypeName("Cardio");

        when(trainingTypeDao.findById(trainingTypeId)).thenReturn(Optional.of(trainingType));
        when(usernameGenerator.generateUsername(firstName, lastName)).thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("password123");

        Trainer savedTrainer = new Trainer();
        savedTrainer.setId(1L);
        savedTrainer.setFirstName(firstName);
        savedTrainer.setLastName(lastName);
        savedTrainer.setUsername("John.Doe");
        savedTrainer.setPassword("password123");
        savedTrainer.setIsActive(true);
        savedTrainer.setSpecialization(trainingType);

        when(trainerDao.create(any(Trainer.class))).thenReturn(savedTrainer);

        Trainer result = trainerService.createTrainer(firstName, lastName, trainingTypeId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John.Doe", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertEquals(true, result.getIsActive());
        assertEquals(trainingType, result.getSpecialization());

        verify(trainingTypeDao).findById(trainingTypeId);
        verify(usernameGenerator).generateUsername(firstName, lastName);
        verify(passwordGenerator).generatePassword();
        verify(trainerDao).create(any(Trainer.class));
    }

    @Test
    void createTrainer_ThrowExceptionWhenTrainingTypeNotFound() {
        Long trainingTypeId = 999L;
        when(trainingTypeDao.findById(trainingTypeId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainerService.createTrainer("John", "Doe", trainingTypeId)
        );

        verify(trainingTypeDao).findById(trainingTypeId);
        verify(trainerDao, never()).create(any(Trainer.class));
    }

    @Test
    void updateTrainer_UpdateExistingTrainer() {
        Long id = 1L;
        String firstName = "Jane";
        String lastName = "Doe";
        Long trainingTypeId = 2L;
        Boolean isActive = false;

        TrainingType trainingType = new TrainingType();
        trainingType.setId(trainingTypeId);
        trainingType.setTrainingTypeName("Strength");

        Trainer existingTrainer = new Trainer();
        existingTrainer.setId(id);
        existingTrainer.setFirstName("John");
        existingTrainer.setLastName("Smith");

        when(trainingTypeDao.findById(trainingTypeId)).thenReturn(Optional.of(trainingType));
        when(trainerDao.findById(id)).thenReturn(Optional.of(existingTrainer));

        Trainer updatedTrainer = new Trainer();
        updatedTrainer.setId(id);
        updatedTrainer.setFirstName(firstName);
        updatedTrainer.setLastName(lastName);
        updatedTrainer.setSpecialization(trainingType);
        updatedTrainer.setIsActive(isActive);

        when(trainerDao.update(any(Trainer.class))).thenReturn(updatedTrainer);

        Trainer result = trainerService.updateTrainer(id, firstName, lastName, trainingTypeId, isActive);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(firstName, result.getFirstName());
        assertEquals(lastName, result.getLastName());
        assertEquals(trainingType, result.getSpecialization());
        assertEquals(isActive, result.getIsActive());

        verify(trainingTypeDao).findById(trainingTypeId);
        verify(trainerDao).findById(id);
        verify(trainerDao).update(any(Trainer.class));
    }

    @Test
    void updateTrainer_ThrowExceptionWhenTrainerNotFound() {
        Long id = 999L;
        Long trainingTypeId = 1L;

        TrainingType trainingType = new TrainingType();
        trainingType.setId(trainingTypeId);

        when(trainingTypeDao.findById(trainingTypeId)).thenReturn(Optional.of(trainingType));
        when(trainerDao.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainerService.updateTrainer(id, "John", "Doe", trainingTypeId, true)
        );

        verify(trainingTypeDao).findById(trainingTypeId);
        verify(trainerDao).findById(id);
        verify(trainerDao, never()).update(any(Trainer.class));
    }

    @Test
    void updateTrainer_ThrowExceptionWhenTrainingTypeNotFound() {
        Long id = 1L;
        Long trainingTypeId = 999L;

        when(trainingTypeDao.findById(trainingTypeId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainerService.updateTrainer(id, "John", "Doe", trainingTypeId, true)
        );

        verify(trainingTypeDao).findById(trainingTypeId);
        verify(trainerDao, never()).findById(any());
        verify(trainerDao, never()).update(any(Trainer.class));
    }

    @Test
    void updateTrainer_ThrowExceptionWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                trainerService.updateTrainer(null, "John", "Doe", 1L, true)
        );
        verify(trainerDao, never()).findById(any());
        verify(trainerDao, never()).update(any());
    }

    @Test
    void getTrainerById_DelegatesToDao() {
        trainerService.getTrainerById(1L);
        verify(trainerDao).findById(1L);
    }

    @Test
    void getTrainerById_ReturnEmptyWhenIdIsNull() {
        Optional<Trainer> result = trainerService.getTrainerById(null);

        assertFalse(result.isPresent());
        verify(trainerDao, never()).findById(any());
    }

    @Test
    void getTrainerByUsername_DelegatesToDao() {
        trainerService.getTrainerByUsername("John.Doe");
        verify(trainerDao).findByUsername("John.Doe");
    }

    @Test
    void getTrainerByUsername_ReturnEmptyWhenUsernameIsNull() {
        Optional<Trainer> result = trainerService.getTrainerByUsername(null);

        assertFalse(result.isPresent());
        verify(trainerDao, never()).findByUsername(any());
    }

    @Test
    void getAllTrainers_DelegatesToDao() {
        trainerService.getAllTrainers();
        verify(trainerDao).findAll();
    }
}
