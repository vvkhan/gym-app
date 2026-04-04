package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.AddTrainingRequest;
import com.epam.gym.core.facade.GymFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    public TrainingController(GymFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @Operation(summary = "Add training session")
    @ApiResponse(responseCode = "200", description = "Training added successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee or trainer not found")
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest body) {
        facade.createTraining(
                body.getTraineeUsername(), body.getTrainerUsername(),
                body.getTrainingName(), body.getTrainingDate(), body.getDuration());
        return ResponseEntity.ok().build();
    }
}
