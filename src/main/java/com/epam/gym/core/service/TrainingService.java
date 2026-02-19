package com.epam.gym.core.service;

import com.epam.gym.core.dao.TraineeDao;
import com.epam.gym.core.dao.TrainerDao;
import com.epam.gym.core.dao.TrainingDao;
import com.epam.gym.core.dao.TrainingTypeDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                  Long trainingTypeId, LocalDate trainingDate, Integer duration) {
        traineeDao.findById(traineeId)
                .orElseThrow(() -> new NoSuchElementException("Trainee with id " + traineeId + " not found"));
        trainerDao.findById(trainerId)
                .orElseThrow(() -> new NoSuchElementException("Trainer with id " + trainerId + " not found"));
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new NoSuchElementException("TrainingType with id " + trainingTypeId + " not found"));

        return trainingDao.create(buildTraining(traineeId, trainerId, trainingName, trainingType, trainingDate, duration));
    }

    public Optional<Training> getTrainingById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return trainingDao.findById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingDao.findAll();
    }

    public List<Training> getTrainingsByTrainee(Long traineeId) {
        if (traineeId == null) {
            return Collections.emptyList();
        }
        return trainingDao.findByTraineeId(traineeId);
    }

    public List<Training> getTrainingsByTrainer(Long trainerId) {
        if (trainerId == null) {
            return Collections.emptyList();
        }
        return trainingDao.findByTrainerId(trainerId);
    }

    public List<TrainingType> getAllTrainingTypes() {
        return trainingTypeDao.findAll();
    }

    // Helper

    private Training buildTraining(Long traineeId, Long trainerId, String trainingName,
                                  TrainingType trainingType, LocalDate trainingDate, Integer duration) {
        return Training.builder()
                .traineeId(traineeId)
                .trainerId(trainerId)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(trainingDate)
                .duration(duration)
                .build();
    }
}
