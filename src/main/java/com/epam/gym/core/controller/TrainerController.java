package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.ActivateDeactivateRequest;
import com.epam.gym.core.dto.request.TrainerRegistrationRequest;
import com.epam.gym.core.dto.request.UpdateTrainerRequest;
import com.epam.gym.core.dto.response.RegistrationResponse;
import com.epam.gym.core.dto.response.TrainerProfileResponse;
import com.epam.gym.core.dto.response.TrainingResponse;
import com.epam.gym.core.dto.response.UpdatedTrainerProfileResponse;
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
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainers", description = "Trainer registration and profile management")
public class TrainerController {

    private final GymFacade facade;
    private final AuthenticationHelper authHelper;

    public TrainerController(GymFacade facade, AuthenticationHelper authHelper) {
        this.facade = facade;
        this.authHelper = authHelper;
    }

    @PostMapping
    @Operation(summary = "Register trainer")
    @ApiResponse(responseCode = "201", description = "Trainer registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        RegistrationResponse response = facade.registerTrainer(
                request.getFirstName(), request.getLastName(), request.getSpecializationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile")
    @ApiResponse(responseCode = "200", description = "Profile returned successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @Parameter(description = "Trainer username") @PathVariable String username,
            HttpServletRequest request) {
        authHelper.authenticate(request, username);
        return ResponseEntity.ok(facade.getTrainerByUsername(username));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<UpdatedTrainerProfileResponse> updateProfile(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Valid @RequestBody UpdateTrainerRequest body,
            HttpServletRequest request) {
        authHelper.authenticate(request, username);
        return ResponseEntity.ok(facade.updateTrainerProfile(
                username, body.getFirstName(), body.getLastName(), body.getIsActive()));
    }

    @PatchMapping("/{username}/activate")
    @Operation(summary = "Activate or deactivate trainer")
    @ApiResponse(responseCode = "200", description = "Status changed successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    @ApiResponse(responseCode = "409", description = "Trainer already in requested state")
    public ResponseEntity<Void> activate(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest body,
            HttpServletRequest request) {
        authHelper.authenticate(request, username);
        if (Boolean.TRUE.equals(body.getIsActive())) {
            facade.activateTrainer(username);
        } else {
            facade.deactivateTrainer(username);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer's training list")
    @ApiResponse(responseCode = "200", description = "Trainings returned successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeUsername,
            HttpServletRequest request) {
        authHelper.authenticate(request, username);
        return ResponseEntity.ok(facade.getTrainerTrainings(
                username, periodFrom, periodTo, traineeUsername));
    }

}
