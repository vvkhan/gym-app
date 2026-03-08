package com.epam.gym.core.service;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.repository.TraineeRepository;
import com.epam.gym.core.repository.TrainerRepository;
import com.epam.gym.core.repository.TrainingRepository;
import com.epam.gym.core.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createTraining_CreateWithEntityReferences() {
        UUID traineeId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();

        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Cardio");

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(typeId)).thenReturn(Optional.of(type));

        Training saved = Training.builder()
                .trainee(trainee).trainer(trainer)
                .trainingName("Morning Cardio").trainingType(type)
                .trainingDate(LocalDate.of(2025, 1, 10)).duration(60)
                .build();
        when(trainingRepository.save(any(Training.class))).thenReturn(saved);

        Training result = trainingService.createTraining(traineeId, trainerId, "Morning Cardio",
                typeId, LocalDate.of(2025, 1, 10), 60);

        assertNotNull(result);
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());
        assertEquals(type, result.getTrainingType());
        assertEquals("Morning Cardio", result.getTrainingName());
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void createTraining_ThrowWhenTraineeNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(traineeRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainingService.createTraining(unknownId, UUID.randomUUID(), "Training",
                        UUID.randomUUID(), LocalDate.now(), 60));

        verify(trainerRepository, never()).findById(any());
        verify(trainingTypeRepository, never()).findById(any());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createTraining_ThrowWhenTrainerNotFound() {
        UUID traineeId = UUID.randomUUID();
        UUID unknownId = UUID.randomUUID();
        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(new Trainee()));
        when(trainerRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainingService.createTraining(traineeId, unknownId, "Training",
                        UUID.randomUUID(), LocalDate.now(), 60));

        verify(trainingTypeRepository, never()).findById(any());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createTraining_ThrowWhenTrainingTypeNotFound() {
        UUID traineeId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();
        UUID unknownId = UUID.randomUUID();
        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(new Trainee()));
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(new Trainer()));
        when(trainingTypeRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                trainingService.createTraining(traineeId, trainerId, "Training",
                        unknownId, LocalDate.now(), 60));

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void getTrainingById_ReturnEmptyWhenIdIsNull() {
        assertFalse(trainingService.getTrainingById(null).isPresent());
        verify(trainingRepository, never()).findById(any());
    }

}
