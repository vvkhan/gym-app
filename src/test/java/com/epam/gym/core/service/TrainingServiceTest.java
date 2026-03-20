package com.epam.gym.core.service;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.repository.TrainingRepository;
import com.epam.gym.core.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createTraining_SavesAndReturnsTraining() {
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Cardio");

        Training saved = Training.builder()
                .trainee(trainee).trainer(trainer)
                .trainingName("Morning Cardio").trainingType(type)
                .trainingDate(LocalDate.of(2025, 1, 10)).duration(60)
                .build();
        when(trainingRepository.save(any(Training.class))).thenReturn(saved);

        Training result = trainingService.createTraining(trainee, trainer, "Morning Cardio",
                type, LocalDate.of(2025, 1, 10), 60);

        assertNotNull(result);
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());
        assertEquals(type, result.getTrainingType());
        assertEquals("Morning Cardio", result.getTrainingName());
        verify(trainingRepository).save(any(Training.class));
    }
}
