package com.epam.gym.core.facade;

import com.epam.gym.core.aspect.LogExecution;
import com.epam.gym.core.client.ReportNotificationService;
import com.epam.gym.core.dto.request.ActionType;
import com.epam.gym.core.dto.request.WorkloadRequest;
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
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.User;
import com.epam.gym.core.service.TraineeService;
import com.epam.gym.core.service.TrainerService;
import com.epam.gym.core.service.TrainingService;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
    private final ReportNotificationService reportNotificationService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     TraineeMapper traineeMapper,
                     TrainerMapper trainerMapper,
                     TrainerSummaryMapper trainerSummaryMapper,
                     TrainingMapper trainingMapper,
                     TrainingTypeMapper trainingTypeMapper,
                     ReportNotificationService reportNotificationService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainerSummaryMapper = trainerSummaryMapper;
        this.trainingMapper = trainingMapper;
        this.trainingTypeMapper = trainingTypeMapper;
        this.reportNotificationService = reportNotificationService;
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

    // Deletes trainee and notifies the report service for every training that cascade deleted
    public void deleteTrainee(String username) {
        List<Training> trainings = traineeService.getTrainingsByUsername(username);
        traineeService.deleteTrainee(username);
        trainings.forEach(t -> notifyReportService(t, ActionType.DELETE));
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
        return trainingMapper.toTraineeViewResponse(
                traineeService.getTrainings(username, fromDate, toDate, trainerUsername, trainingTypeName));
    }

    public List<TrainerSummaryResponse> getNotAssignedTrainers(String traineeUsername) {
        return trainerSummaryMapper.toDto(traineeService.getNotAssignedTrainers(traineeUsername));
    }

    public List<TrainerSummaryResponse> updateTraineeTrainers(String traineeUsername,
                                                              Set<String> trainerUsernames) {
        Trainee updated = traineeService.updateTrainers(traineeUsername, trainerUsernames);
        return trainerSummaryMapper.toDto(new ArrayList<>(updated.getTrainers()));
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
        return trainingMapper.toTrainerViewResponse(
                trainerService.getTrainings(username, fromDate, toDate, traineeUsername));
    }

    // -------------------------------------------------------------------------
    // Training operations
    // -------------------------------------------------------------------------

    // Creates a training and notifies the report service with an ADD event
    public void createTraining(String traineeUsername, String trainerUsername, String trainingName,
                               LocalDate trainingDate, Integer duration) {
        Trainee trainee = traineeService.getTraineeByUsername(traineeUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + traineeUsername));
        Trainer trainer = trainerService.getTrainerByUsername(trainerUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + trainerUsername));
        Training saved = trainingService.createTraining(trainee, trainer, trainingName,
                trainer.getSpecialization(), trainingDate, duration);
        notifyReportService(saved, ActionType.ADD);
    }

    public List<TrainingTypeResponse> getAllTrainingTypes() {
        return trainingTypeMapper.toDto(trainingService.getAllTrainingTypes());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Sends a workload event to the report service.
     *
     * The transactionId from MDC is forwarded as X-Transaction-Id so the report
     * service logs carry the same txId as this gym-core request — enabling
     * cross-service log correlation (requirement 9).
     *
     * If the report service is unreachable the circuit breaker routes to
     * ReportServiceClientFallback, which logs the dropped event and returns
     * 200 so the caller is unaffected.
     */
    private void notifyReportService(Training training, ActionType action) {
        String txId = Optional.ofNullable(MDC.get("transactionId")).orElse("no-tx");
        User trainerUser = training.getTrainer().getUser();

        WorkloadRequest request = new WorkloadRequest(
                trainerUser.getUsername(),
                trainerUser.getFirstName(),
                trainerUser.getLastName(),
                trainerUser.getIsActive(),
                training.getTrainingDate(),
                training.getDuration(),
                action
        );
        reportNotificationService.notify(txId, request);
    }
}
