package com.epam.gym.core.facade;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.service.impl.TraineeService;
import com.epam.gym.core.service.impl.TrainerService;
import com.epam.gym.core.service.impl.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Does not bring any business logic value but required within the task
 */
@Component
public class GymFacade {

    private static final Logger log = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // Trainee operations

    public Trainee registerTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        log.info("Registering trainee: {} {}", firstName, lastName);
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    public Trainee updateTraineeProfile(Long id, String firstName, String lastName, LocalDate dateOfBirth,
                                        String address, Boolean isActive) {
        log.info("Updating trainee profile: {}", id);
        return traineeService.updateTrainee(id, firstName, lastName, dateOfBirth, address, isActive);
    }

    public void deleteTrainee(Long id) {
        log.info("Deleting trainee: {}", id);
        traineeService.deleteTrainee(id);
    }

    public Optional<Trainee> getTrainee(Long id) {
        log.debug("Getting trainee: {}", id);
        return traineeService.getTraineeById(id);
    }

    public Optional<Trainee> getTraineeByUsername(String username) {
        log.debug("Getting trainee by username: {}", username);
        return traineeService.getTraineeByUsername(username);
    }

    public List<Trainee> getAllTrainees() {
        log.debug("Getting all trainees");
        return traineeService.getAllTrainees();
    }

    // Trainer operations

    public Trainer registerTrainer(String firstName, String lastName, Long trainingTypeId) {
        log.info("Registering trainer: {} {}", firstName, lastName);
        return trainerService.createTrainer(firstName, lastName, trainingTypeId);
    }

    public Trainer updateTrainerProfile(Long id, String firstName, String lastName, Long trainingTypeId, Boolean isActive) {
        log.info("Updating trainer profile: {}", id);
        return trainerService.updateTrainer(id, firstName, lastName, trainingTypeId, isActive);
    }

    public Optional<Trainer> getTrainer(Long id) {
        log.debug("Getting trainer: {}", id);
        return trainerService.getTrainerById(id);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        log.debug("Getting trainer by username: {}", username);
        return trainerService.getTrainerByUsername(username);
    }

    public List<Trainer> getAllTrainers() {
        log.debug("Getting all trainers");
        return trainerService.getAllTrainers();
    }

    // Training operations

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   Long trainingTypeId, LocalDate trainingDate, Integer duration) {
        log.info("Creating training: {} for trainee {} and trainer {}", trainingName, traineeId, trainerId);
        return trainingService.createTraining(traineeId, trainerId, trainingName, trainingTypeId, trainingDate, duration);
    }

    public Optional<Training> getTraining(Long id) {
        log.debug("Getting training: {}", id);
        return trainingService.getTrainingById(id);
    }

    public List<Training> getAllTrainings() {
        log.debug("Getting all trainings");
        return trainingService.getAllTrainings();
    }

    public List<Training> getTraineeTrainings(Long traineeId) {
        log.debug("Getting trainings for trainee: {}", traineeId);
        return trainingService.getTrainingsByTrainee(traineeId);
    }

    public List<Training> getTrainerTrainings(Long trainerId) {
        log.debug("Getting trainings for trainer: {}", trainerId);
        return trainingService.getTrainingsByTrainer(trainerId);
    }

    // Utility operation
    public List<TrainingType> getAllTrainingTypes() {
        log.debug("Getting all training types");
        return trainingService.getAllTrainingTypes();
    }
}
