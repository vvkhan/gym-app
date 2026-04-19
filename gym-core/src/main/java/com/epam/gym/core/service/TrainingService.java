package com.epam.gym.core.service;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.repository.TrainingRepository;
import com.epam.gym.core.repository.TrainingTypeRepository;
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@LogExecution
@Service
public class TrainingService {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Transactional
    public Training createTraining(Trainee trainee, Trainer trainer, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate, Integer duration) {
        return trainingRepository.save(buildTraining(trainee, trainer, trainingName, trainingType, trainingDate, duration));
    }

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
