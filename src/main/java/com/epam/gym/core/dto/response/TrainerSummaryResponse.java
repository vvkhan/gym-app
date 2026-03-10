package com.epam.gym.core.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrainerSummaryResponse {

    private String username;
    private String firstName;
    private String lastName;
    private TrainingTypeResponse specialization;
}
