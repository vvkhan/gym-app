package com.epam.gym.core.facade;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.aspect.LogExecution;
import com.epam.gym.core.service.TraineeService;
import com.epam.gym.core.service.TrainerService;
import com.epam.gym.core.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@LogExecution
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

    // Authentication

    public boolean authenticateTrainee(String username, String password) {
        return traineeService.authenticate(username, password);
    }

    public boolean authenticateTrainer(String username, String password) {
        return trainerService.authenticate(username, password);
    }

    // Trainee operations

    public Trainee registerTrainee(String firstName, String lastName,
                                   LocalDate dateOfBirth, String address) {
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    public Trainee updateTraineeProfile(String username, String password,
                                        String firstName, String lastName,
                                        LocalDate dateOfBirth, String address, Boolean isActive) {
        requireTraineeAuth(username, password);
        return traineeService.updateTrainee(username, firstName, lastName, dateOfBirth, address, isActive);
    }

    public void deleteTrainee(String username, String password) {
        requireTraineeAuth(username, password);
        traineeService.deleteTrainee(username);
    }

    public void changeTraineePassword(String username, String currentPassword, String newPassword) {
        requireTraineeAuth(username, currentPassword);
        traineeService.changePassword(username, currentPassword, newPassword);
    }

    public void activateTrainee(String username, String password) {
        requireTraineeAuth(username, password);
        traineeService.activate(username);
    }

    public void deactivateTrainee(String username, String password) {
        requireTraineeAuth(username, password);
        traineeService.deactivate(username);
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

    public List<Training> getTraineeTrainings(String username, String password,
                                              LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingTypeName) {
        requireTraineeAuth(username, password);
        return traineeService.getTrainings(username, fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Trainer> getNotAssignedTrainers(String traineeUsername, String password) {
        requireTraineeAuth(traineeUsername, password);
        return traineeService.getNotAssignedTrainers(traineeUsername);
    }

    public Trainee updateTraineeTrainers(String traineeUsername, String password,
                                         List<String> trainerUsernames) {
        requireTraineeAuth(traineeUsername, password);
        return traineeService.updateTrainers(traineeUsername, trainerUsernames);
    }

    // Trainer operations

    public Trainer registerTrainer(String firstName, String lastName, Long trainingTypeId) {
        return trainerService.createTrainer(firstName, lastName, trainingTypeId);
    }

    public Trainer updateTrainerProfile(String username, String password,
                                        String firstName, String lastName,
                                        Long trainingTypeId, Boolean isActive) {
        requireTrainerAuth(username, password);
        return trainerService.updateTrainer(username, firstName, lastName, trainingTypeId, isActive);
    }

    public void changeTrainerPassword(String username, String currentPassword, String newPassword) {
        requireTrainerAuth(username, currentPassword);
        trainerService.changePassword(username, currentPassword, newPassword);
    }

    public void activateTrainer(String username, String password) {
        requireTrainerAuth(username, password);
        trainerService.activate(username);
    }

    public void deactivateTrainer(String username, String password) {
        requireTrainerAuth(username, password);
        trainerService.deactivate(username);
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

    public List<Training> getTrainerTrainings(String username, String password,
                                              LocalDate fromDate, LocalDate toDate,
                                              String traineeName) {
        requireTrainerAuth(username, password);
        return trainerService.getTrainings(username, fromDate, toDate, traineeName);
    }

    // Training operations

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   Long trainingTypeId, LocalDate trainingDate, Integer duration) {
        return trainingService.createTraining(traineeId, trainerId, trainingName,
                trainingTypeId, trainingDate, duration);
    }

    public Optional<Training> getTraining(Long id) {
        return trainingService.getTrainingById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.getAllTrainings();
    }

    public List<TrainingType> getAllTrainingTypes() {
        return trainingService.getAllTrainingTypes();
    }

    // Helper

    private void requireTraineeAuth(String username, String password) {
        if (!traineeService.authenticate(username, password)) {
            throw new SecurityException("Authentication failed for trainee: " + username);
        }
    }

    private void requireTrainerAuth(String username, String password) {
        if (!trainerService.authenticate(username, password)) {
            throw new SecurityException("Authentication failed for trainer: " + username);
        }
    }
}
