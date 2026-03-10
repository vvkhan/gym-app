package com.epam.gym.core.facade;

import com.epam.gym.core.aspect.LogExecution;
import com.epam.gym.core.dto.response.RegistrationResponse;
import com.epam.gym.core.dto.response.TraineeProfileResponse;
import com.epam.gym.core.dto.response.TrainerProfileResponse;
import com.epam.gym.core.dto.response.TrainerSummaryResponse;
import com.epam.gym.core.dto.response.TrainingResponse;
import com.epam.gym.core.dto.response.TrainingTypeResponse;
import com.epam.gym.core.dto.response.UpdatedTraineeProfileResponse;
import com.epam.gym.core.dto.response.UpdatedTrainerProfileResponse;
import com.epam.gym.core.mapper.TraineeMapper;
import com.epam.gym.core.mapper.TrainerMapper;
import com.epam.gym.core.mapper.TrainerSummaryMapper;
import com.epam.gym.core.mapper.TrainingMapper;
import com.epam.gym.core.mapper.TrainingTypeMapper;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.service.TraineeService;
import com.epam.gym.core.service.TrainerService;
import com.epam.gym.core.service.TrainingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@LogExecution
@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainerSummaryMapper trainerSummaryMapper;
    private final TrainingMapper trainingMapper;
    private final TrainingTypeMapper trainingTypeMapper;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     TraineeMapper traineeMapper,
                     TrainerMapper trainerMapper,
                     TrainerSummaryMapper trainerSummaryMapper,
                     TrainingMapper trainingMapper,
                     TrainingTypeMapper trainingTypeMapper) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainerSummaryMapper = trainerSummaryMapper;
        this.trainingMapper = trainingMapper;
        this.trainingTypeMapper = trainingTypeMapper;
    }

    // -------------------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------------------

    public boolean authenticateTrainee(String username, String password) {
        return traineeService.authenticate(username, password);
    }

    public boolean authenticateTrainer(String username, String password) {
        return trainerService.authenticate(username, password);
    }

    // -------------------------------------------------------------------------
    // Trainee operations
    // -------------------------------------------------------------------------

    public RegistrationResponse registerTrainee(String firstName, String lastName,
                                                LocalDate dateOfBirth, String address) {
        Trainee trainee = traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
        return traineeMapper.toRegistrationResponse(trainee);
    }

    public TraineeProfileResponse getTraineeByUsername(String username) {
        return traineeService.getTraineeByUsername(username)
                .map(traineeMapper::toProfileResponse)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));
    }

    public UpdatedTraineeProfileResponse updateTraineeProfile(String username,
                                                              String firstName, String lastName,
                                                              LocalDate dateOfBirth, String address,
                                                              Boolean isActive) {
        Trainee updated = traineeService.updateTrainee(username, firstName, lastName,
                dateOfBirth, address, isActive);
        return traineeMapper.toUpdatedProfileResponse(updated);
    }

    public void deleteTrainee(String username) {
        traineeService.deleteTrainee(username);
    }

    public void changePassword(String username, String newPassword) {
        if (traineeService.getTraineeByUsername(username).isPresent()) {
            traineeService.changePassword(username, newPassword);
        } else if (trainerService.getTrainerByUsername(username).isPresent()) {
            trainerService.changePassword(username, newPassword);
        } else {
            throw new NoSuchElementException("User not found: " + username);
        }
    }

    public void activateTrainee(String username) {
        traineeService.activate(username);
    }

    public void deactivateTrainee(String username) {
        traineeService.deactivate(username);
    }

    public List<TrainingResponse> getTraineeTrainings(String username,
                                                      LocalDate fromDate, LocalDate toDate,
                                                      String trainerUsername, String trainingTypeName) {
        return traineeService.getTrainings(username, fromDate, toDate, trainerUsername, trainingTypeName)
                .stream()
                .map(trainingMapper::toTraineeViewResponse)
                .toList();
    }

    public List<TrainerSummaryResponse> getNotAssignedTrainers(String traineeUsername) {
        return traineeService.getNotAssignedTrainers(traineeUsername)
                .stream()
                .map(trainerSummaryMapper::toDto)
                .toList();
    }

    public List<TrainerSummaryResponse> updateTraineeTrainers(String traineeUsername,
                                                              Set<String> trainerUsernames) {
        Trainee updated = traineeService.updateTrainers(traineeUsername, trainerUsernames);
        return updated.getTrainers()
                .stream()
                .map(trainerSummaryMapper::toDto)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Trainer operations
    // -------------------------------------------------------------------------

    public RegistrationResponse registerTrainer(String firstName, String lastName, UUID trainingTypeId) {
        Trainer trainer = trainerService.createTrainer(firstName, lastName, trainingTypeId);
        return trainerMapper.toRegistrationResponse(trainer);
    }

    public TrainerProfileResponse getTrainerByUsername(String username) {
        return trainerService.getTrainerByUsername(username)
                .map(trainerMapper::toProfileResponse)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));
    }

    public UpdatedTrainerProfileResponse updateTrainerProfile(String username,
                                                              String firstName, String lastName,
                                                              Boolean isActive) {
        Trainer current = trainerService.getTrainerByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));
        Trainer updated = trainerService.updateTrainer(username, firstName, lastName,
                current.getSpecialization().getId(), isActive);
        return trainerMapper.toUpdatedProfileResponse(updated);
    }

    public void activateTrainer(String username) {
        trainerService.activate(username);
    }

    public void deactivateTrainer(String username) {
        trainerService.deactivate(username);
    }

    public List<TrainingResponse> getTrainerTrainings(String username,
                                                      LocalDate fromDate, LocalDate toDate,
                                                      String traineeUsername) {
        return trainerService.getTrainings(username, fromDate, toDate, traineeUsername)
                .stream()
                .map(trainingMapper::toTrainerViewResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Training operations
    // -------------------------------------------------------------------------

    public void createTraining(String traineeUsername, String trainerUsername, String trainingName,
                               LocalDate trainingDate, Integer duration) {
        Trainee trainee = traineeService.getTraineeByUsername(traineeUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + traineeUsername));
        Trainer trainer = trainerService.getTrainerByUsername(trainerUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + trainerUsername));
        trainingService.createTraining(trainee, trainer, trainingName,
                trainer.getSpecialization(), trainingDate, duration);
    }

    public List<TrainingTypeResponse> getAllTrainingTypes() {
        return trainingService.getAllTrainingTypes()
                .stream()
                .map(trainingTypeMapper::toDto)
                .toList();
    }
}
