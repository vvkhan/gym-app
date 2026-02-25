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
import com.epam.gym.core.aspect.LogExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@LogExecution
@Service
public class TrainingService {

    @Autowired
    private TrainingDao trainingDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Transactional
    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   Long trainingTypeId, LocalDate trainingDate, Integer duration) {
        Trainee trainee = traineeDao.findById(traineeId)
                .orElseThrow(() -> new NoSuchElementException("Trainee with id " + traineeId + " not found"));
        Trainer trainer = trainerDao.findById(trainerId)
                .orElseThrow(() -> new NoSuchElementException("Trainer with id " + trainerId + " not found"));
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new NoSuchElementException("TrainingType with id " + trainingTypeId + " not found"));
        return trainingDao.create(buildTraining(trainee, trainer, trainingName, trainingType, trainingDate, duration));
    }

    @Transactional(readOnly = true)
    public Optional<Training> getTrainingById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return trainingDao.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        return trainingDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainingsByTrainee(Long traineeId) {
        if (traineeId == null) {
            return Collections.emptyList();
        }
        return trainingDao.findByTraineeId(traineeId);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainingsByTrainer(Long trainerId) {
        if (trainerId == null) {
            return Collections.emptyList();
        }
        return trainingDao.findByTrainerId(trainerId);
    }

    @Transactional(readOnly = true)
    public List<TrainingType> getAllTrainingTypes() {
        return trainingTypeDao.findAll();
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
