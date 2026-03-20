package com.epam.gym.core.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TrainingResponse {

    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer duration;
    private String partnerName;
}
