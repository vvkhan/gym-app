package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.AddTrainingRequest;
import com.epam.gym.core.facade.GymFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Trainings", description = "Training session management")
public class TrainingController {

    private final GymFacade facade;
    private final AuthenticationHelper authHelper;

    public TrainingController(GymFacade facade, AuthenticationHelper authHelper) {
        this.facade = facade;
        this.authHelper = authHelper;
    }

    @PostMapping
    @Operation(summary = "Add training session")
    // REST convention is 201 Created (https://restfulapi.net/http-status-201-created/)
    // Set 200 OK as per requirements
    @ApiResponse(responseCode = "200", description = "Training added successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee or trainer not found")
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody AddTrainingRequest body,
            HttpServletRequest request) {
        authHelper.authenticateTrainee(request, body.getTraineeUsername());
        facade.createTraining(
                body.getTraineeUsername(), body.getTrainerUsername(),
                body.getTrainingName(), body.getTrainingDate(), body.getDuration());
        return ResponseEntity.ok().build();
    }

}
