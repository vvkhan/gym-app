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
import java.util.Arrays;
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
    void createTraining_CreateTrainingWhenAllEntitiesExist() {
        Long traineeId = 1L;
        Long trainerId = 2L;
        String trainingName = "Morning Cardio";
        Long trainingTypeId = 3L;
        LocalDate trainingDate = LocalDate.of(2024, 1, 15);
        Integer duration = 60;

        Trainee trainee = new Trainee();
        trainee.setId(traineeId);

        Trainer trainer = new Trainer();
        trainer.setId(trainerId);

        TrainingType trainingType = new TrainingType();
        trainingType.setId(trainingTypeId);
        trainingType.setTrainingTypeName("Cardio");

        Training savedTraining = new Training();
        savedTraining.setId(1L);
        savedTraining.setTraineeId(traineeId);
        savedTraining.setTrainerId(trainerId);
        savedTraining.setTrainingName(trainingName);
        savedTraining.setTrainingType(trainingType);
        savedTraining.setTrainingDate(trainingDate);
        savedTraining.setDuration(duration);

        when(traineeDao.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findById(trainingTypeId)).thenReturn(Optional.of(trainingType));
        when(trainingDao.create(any(Training.class))).thenReturn(savedTraining);

        Training result = trainingService.createTraining(traineeId, trainerId, trainingName,
                                                         trainingTypeId, trainingDate, duration);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(traineeId, result.getTraineeId());
        assertEquals(trainerId, result.getTrainerId());
        assertEquals(trainingName, result.getTrainingName());
        assertEquals(trainingType, result.getTrainingType());
        assertEquals(trainingDate, result.getTrainingDate());
        assertEquals(duration, result.getDuration());

        verify(traineeDao).findById(traineeId);
        verify(trainerDao).findById(trainerId);
        verify(trainingTypeDao).findById(trainingTypeId);
        verify(trainingDao).create(any(Training.class));
    }

    @Test
    void createTraining_ThrowExceptionWhenTraineeNotFound() {
        Long traineeId = 999L;
        Long trainerId = 1L;
        Long trainingTypeId = 1L;

        when(traineeDao.findById(traineeId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainingService.createTraining(traineeId, trainerId, "Training",
                                               trainingTypeId, LocalDate.now(), 60)
        );

        verify(traineeDao).findById(traineeId);
        verify(trainerDao, never()).findById(any());
        verify(trainingTypeDao, never()).findById(any());
        verify(trainingDao, never()).create(any(Training.class));
    }

    @Test
    void createTraining_ThrowExceptionWhenTrainerNotFound() {
        Long traineeId = 1L;
        Long trainerId = 999L;
        Long trainingTypeId = 1L;

        Trainee trainee = new Trainee();
        trainee.setId(traineeId);

        when(traineeDao.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(trainerId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainingService.createTraining(traineeId, trainerId, "Training",
                                               trainingTypeId, LocalDate.now(), 60)
        );

        verify(traineeDao).findById(traineeId);
        verify(trainerDao).findById(trainerId);
        verify(trainingTypeDao, never()).findById(any());
        verify(trainingDao, never()).create(any(Training.class));
    }

    @Test
    void createTraining_ThrowExceptionWhenTrainingTypeNotFound() {
        Long traineeId = 1L;
        Long trainerId = 2L;
        Long trainingTypeId = 999L;

        Trainee trainee = new Trainee();
        trainee.setId(traineeId);

        Trainer trainer = new Trainer();
        trainer.setId(trainerId);

        when(traineeDao.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findById(trainingTypeId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainingService.createTraining(traineeId, trainerId, "Training",
                                               trainingTypeId, LocalDate.now(), 60)
        );

        verify(traineeDao).findById(traineeId);
        verify(trainerDao).findById(trainerId);
        verify(trainingTypeDao).findById(trainingTypeId);
        verify(trainingDao, never()).create(any(Training.class));
    }

    @Test
    void getTrainingById_DelegatesToDao() {
        trainingService.getTrainingById(1L);
        verify(trainingDao).findById(1L);
    }

    @Test
    void getTrainingById_ReturnEmptyWhenIdIsNull() {
        Optional<Training> result = trainingService.getTrainingById(null);

        assertFalse(result.isPresent());
        verify(trainingDao, never()).findById(any());
    }

    @Test
    void getAllTrainings_DelegatesToDao() {
        trainingService.getAllTrainings();
        verify(trainingDao).findAll();
    }

    @Test
    void getTrainingsByTrainee_DelegatesToDao() {
        trainingService.getTrainingsByTrainee(1L);
        verify(trainingDao).findByTraineeId(1L);
    }

    @Test
    void getTrainingsByTrainee_ReturnEmptyListWhenTraineeIdIsNull() {
        List<Training> result = trainingService.getTrainingsByTrainee(null);

        assertTrue(result.isEmpty());
        verify(trainingDao, never()).findByTraineeId(any());
    }

    @Test
    void getTrainingsByTrainer_DelegatesToDao() {
        trainingService.getTrainingsByTrainer(1L);
        verify(trainingDao).findByTrainerId(1L);
    }

    @Test
    void getTrainingsByTrainer_ReturnEmptyListWhenTrainerIdIsNull() {
        List<Training> result = trainingService.getTrainingsByTrainer(null);

        assertTrue(result.isEmpty());
        verify(trainingDao, never()).findByTrainerId(any());
    }
}
