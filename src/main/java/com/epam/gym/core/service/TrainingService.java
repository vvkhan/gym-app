package com.epam.gym.core.service;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.repository.TraineeRepository;
import com.epam.gym.core.repository.TrainerRepository;
import com.epam.gym.core.repository.TrainingRepository;
import com.epam.gym.core.repository.TrainingTypeRepository;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@LogExecution
@Service
public class TrainingService {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Transactional
    public Training createTraining(UUID traineeId, UUID trainerId, String trainingName,
                                   UUID trainingTypeId, LocalDate trainingDate, Integer duration) {
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new NoSuchElementException("Trainee with id " + traineeId + " not found"));
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoSuchElementException("Trainer with id " + trainerId + " not found"));
        TrainingType trainingType = trainingTypeRepository.findById(trainingTypeId)
                .orElseThrow(() -> new NoSuchElementException("TrainingType with id " + trainingTypeId + " not found"));
        return trainingRepository.save(buildTraining(trainee, trainer, trainingName, trainingType, trainingDate, duration));
    }

    @Transactional(readOnly = true)
    public Optional<Training> getTrainingById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return trainingRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        return trainingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TrainingType> getAllTrainingTypes() {
        return trainingTypeRepository.findAll();
    }

    // Helper

    private Training buildTraining(Trainee trainee, Trainer trainer, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate, Integer duration) {
        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(trainingDate)
                .duration(duration)
                .build();
    }
}
