package com.epam.gym.core.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdatedTrainerProfileResponse {

    private String username;
    private String firstName;
    private String lastName;
    private TrainingTypeResponse specialization;
    private Boolean isActive;
    private List<TraineeSummaryResponse> trainees;
}
