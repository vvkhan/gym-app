package com.epam.gym.core.service;

import com.epam.gym.core.dao.TraineeDao;
import com.epam.gym.core.dao.TrainerDao;
import com.epam.gym.core.dao.TrainingDao;
import com.epam.gym.core.dao.TrainingTypeDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createTraining_CreateWithEntityReferences() {
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Cardio");

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findById(3L)).thenReturn(Optional.of(type));

        Training saved = Training.builder()
                .trainee(trainee).trainer(trainer)
                .trainingName("Morning Cardio").trainingType(type)
                .trainingDate(LocalDate.of(2025, 1, 10)).duration(60)
                .build();
        when(trainingDao.create(any(Training.class))).thenReturn(saved);

        Training result = trainingService.createTraining(1L, 2L, "Morning Cardio", 3L,
                LocalDate.of(2025, 1, 10), 60);

        assertNotNull(result);
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());
        assertEquals(type, result.getTrainingType());
        assertEquals("Morning Cardio", result.getTrainingName());
        verify(trainingDao).create(any(Training.class));
    }

    @Test
    void createTraining_ThrowWhenTraineeNotFound() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainingService.createTraining(999L, 1L, "Training", 1L, LocalDate.now(), 60));

        verify(trainerDao, never()).findById(any());
        verify(trainingTypeDao, never()).findById(any());
        verify(trainingDao, never()).create(any());
    }

    @Test
    void createTraining_ThrowWhenTrainerNotFound() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainingService.createTraining(1L, 999L, "Training", 1L, LocalDate.now(), 60));

        verify(trainingTypeDao, never()).findById(any());
        verify(trainingDao, never()).create(any());
    }

    @Test
    void createTraining_ThrowWhenTrainingTypeNotFound() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(new Trainer()));
        when(trainingTypeDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainingService.createTraining(1L, 2L, "Training", 999L, LocalDate.now(), 60));

        verify(trainingDao, never()).create(any());
    }

    @Test
    void getTrainingById_ReturnEmptyWhenIdIsNull() {
        assertFalse(trainingService.getTrainingById(null).isPresent());
        verify(trainingDao, never()).findById(any());
    }

    @Test
    void getTrainingsByTrainee_ReturnEmptyListWhenIdIsNull() {
        List<Training> result = trainingService.getTrainingsByTrainee(null);
        assertTrue(result.isEmpty());
        verify(trainingDao, never()).findByTraineeId(any());
    }

    @Test
    void getTrainingsByTrainer_ReturnEmptyListWhenIdIsNull() {
        List<Training> result = trainingService.getTrainingsByTrainer(null);
        assertTrue(result.isEmpty());
        verify(trainingDao, never()).findByTrainerId(any());
    }

}
