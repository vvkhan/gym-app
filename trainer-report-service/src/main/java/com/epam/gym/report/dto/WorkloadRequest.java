package com.epam.gym.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class WorkloadRequest {

    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private boolean isActive;

    @NotNull
    private LocalDate trainingDate;

    @Positive
    private int trainingDurationMinutes;

    @NotNull
    private ActionType actionType;
}
