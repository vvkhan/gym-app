package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.ActivateDeactivateRequest;
import com.epam.gym.core.dto.request.TraineeRegistrationRequest;
import com.epam.gym.core.dto.request.TraineeTrainersUpdateRequest;
import com.epam.gym.core.dto.request.UpdateTraineeRequest;
import com.epam.gym.core.dto.response.RegistrationResponse;
import com.epam.gym.core.dto.response.TraineeProfileResponse;
import com.epam.gym.core.dto.response.TrainerSummaryResponse;
import com.epam.gym.core.dto.response.TrainingResponse;
import com.epam.gym.core.dto.response.UpdatedTraineeProfileResponse;
import com.epam.gym.core.facade.GymFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainees", description = "Trainee registration and profile management")
public class TraineeController {

    private final GymFacade facade;
    private final AuthenticationHelper authHelper;

    public TraineeController(GymFacade facade, AuthenticationHelper authHelper) {
        this.facade = facade;
        this.authHelper = authHelper;
    }

    @PostMapping
    @Operation(summary = "Register trainee")
    @ApiResponse(responseCode = "201", description = "Trainee registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request) {
        RegistrationResponse response = facade.registerTrainee(
                request.getFirstName(), request.getLastName(),
                request.getDateOfBirth(), request.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile")
    @ApiResponse(responseCode = "200", description = "Profile returned successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @Parameter(description = "Trainee username") @PathVariable String username,
            HttpServletRequest request) {
        authHelper.authenticateOwner(request, username);
        return ResponseEntity.ok(facade.getTraineeByUsername(username));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<UpdatedTraineeProfileResponse> updateProfile(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest body,
            HttpServletRequest request) {
        authHelper.authenticateOwner(request, username);
        return ResponseEntity.ok(facade.updateTraineeProfile(
                username, body.getFirstName(), body.getLastName(),
                body.getDateOfBirth(), body.getAddress(), body.getIsActive()));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile")
    // Shouldn't it be 204 No content? Set 200 OK as per requirements
    @ApiResponse(responseCode = "200", description = "Trainee deleted successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Trainee username") @PathVariable String username,
            HttpServletRequest request) {
        authHelper.authenticateOwner(request, username);
        facade.deleteTrainee(username);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/activate")
    @Operation(summary = "Activate or deactivate trainee")
    @ApiResponse(responseCode = "200", description = "Status changed successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    @ApiResponse(responseCode = "409", description = "Trainee already in requested state")
    public ResponseEntity<Void> activate(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest body,
            HttpServletRequest request) {
        authHelper.authenticateOwner(request, username);
        if (body.getIsActive()) {
            facade.activateTrainee(username);
        } else {
            facade.deactivateTrainee(username);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/not-assigned-trainers")
    @Operation(summary = "Get active trainers not assigned to trainee")
    @ApiResponse(responseCode = "200", description = "Trainers returned successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<List<TrainerSummaryResponse>> getNotAssignedTrainers(
            @Parameter(description = "Trainee username") @PathVariable String username,
            HttpServletRequest request) {
        authHelper.authenticateOwner(request, username);
        return ResponseEntity.ok(facade.getNotAssignedTrainers(username));
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainee's trainer list")
    @ApiResponse(responseCode = "200", description = "Trainer list updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee or trainer not found")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTrainers(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody TraineeTrainersUpdateRequest body,
            HttpServletRequest request) {
        authHelper.authenticateOwner(request, username);
        return ResponseEntity.ok(facade.updateTraineeTrainers(
                username, new HashSet<>(body.getTrainerUsernames())));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee's training list")
    @ApiResponse(responseCode = "200", description = "Trainings returned successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerUsername,
            @RequestParam(required = false) String trainingType,
            HttpServletRequest request) {
        authHelper.authenticateOwner(request, username);
        return ResponseEntity.ok(facade.getTraineeTrainings(
                username, periodFrom, periodTo, trainerUsername, trainingType));
    }

}
