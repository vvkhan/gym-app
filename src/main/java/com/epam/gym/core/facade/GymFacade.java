package com.epam.gym.core.facade;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.service.TraineeService;
import com.epam.gym.core.service.TrainerService;
import com.epam.gym.core.service.TrainingService;
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

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // Trainee operations

    public Trainee registerTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    public Trainee updateTraineeProfile(Long id, String firstName, String lastName, LocalDate dateOfBirth,
                                        String address, Boolean isActive) {
        if (id == null) {
            return null;
        }
        return traineeService.updateTrainee(id, firstName, lastName, dateOfBirth, address, isActive);
    }

    public void deleteTrainee(Long id) {
        if (id == null) {
            return;
        }
        traineeService.deleteTrainee(id);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeService.getTraineeById(id);
    }

    public Optional<Trainee> getTraineeByUsername(String username) {
        return traineeService.getTraineeByUsername(username);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.getAllTrainees();
    }

    // Trainer operations

    public Trainer registerTrainer(String firstName, String lastName, Long trainingTypeId) {
        return trainerService.createTrainer(firstName, lastName, trainingTypeId);
    }

    public Trainer updateTrainerProfile(Long id, String firstName, String lastName, Long trainingTypeId, Boolean isActive) {
        if (id == null) {
            return null;
        }
        return trainerService.updateTrainer(id, firstName, lastName, trainingTypeId, isActive);
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerService.getTrainerById(id);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        return trainerService.getTrainerByUsername(username);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.getAllTrainers();
    }

    // Training operations

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   Long trainingTypeId, LocalDate trainingDate, Integer duration) {
        return trainingService.createTraining(traineeId, trainerId, trainingName, trainingTypeId, trainingDate, duration);
    }

    public Optional<Training> getTraining(Long id) {
        return trainingService.getTrainingById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.getAllTrainings();
    }

    public List<Training> getTraineeTrainings(Long traineeId) {
        return trainingService.getTrainingsByTrainee(traineeId);
    }

    public List<Training> getTrainerTrainings(Long trainerId) {
        return trainingService.getTrainingsByTrainer(trainerId);
    }

    // Utility operation
    public List<TrainingType> getAllTrainingTypes() {
        return trainingService.getAllTrainingTypes();
    }
}
