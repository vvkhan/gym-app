package com.epam.gym.core.controller;

import com.epam.gym.core.dto.response.TrainingTypeResponse;
import com.epam.gym.core.facade.GymFacade;
import com.epam.gym.core.util.CredentialsExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training Types", description = "Reference data for training types")
public class TrainingTypeController {

    private final GymFacade facade;

    public TrainingTypeController(GymFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    @Operation(summary = "Get all training types")
    @ApiResponse(responseCode = "200", description = "Training types returned successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<List<TrainingTypeResponse>> getAll(HttpServletRequest request) {
        String[] credentials = CredentialsExtractor.extractCredentials(request);
        boolean ok = facade.authenticateTrainee(credentials[0], credentials[1])
                || facade.authenticateTrainer(credentials[0], credentials[1]);
        if (!ok) {
            throw new SecurityException("Invalid username or password");
        }
        return ResponseEntity.ok(facade.getAllTrainingTypes());
    }
}
