package com.epam.gym.core.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * DTO sent to trainer-report-service on every training ADD or DELETE
 * Includes all trainer info for report service to update monthly summary
 */
@Getter
@AllArgsConstructor
public class WorkloadRequest {

    private String trainerUsername;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private LocalDate trainingDate;
    private int trainingDurationMinutes;
    private ActionType actionType;
}
