package com.epam.gym.core.controller;

import com.epam.gym.core.dto.response.TrainingTypeResponse;
import com.epam.gym.core.facade.GymFacade;
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
    private final AuthenticationHelper authHelper;

    public TrainingTypeController(GymFacade facade, AuthenticationHelper authHelper) {
        this.facade = facade;
        this.authHelper = authHelper;
    }

    @GetMapping
    @Operation(summary = "Get all training types")
    @ApiResponse(responseCode = "200", description = "Training types returned successfully")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<List<TrainingTypeResponse>> getAll(HttpServletRequest request) {
        authHelper.authenticateAny(request);
        return ResponseEntity.ok(facade.getAllTrainingTypes());
    }
}
