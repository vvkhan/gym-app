package com.epam.gym.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TraineeTrainersUpdateRequest {

    @NotEmpty
    private List<@NotBlank String> trainerUsernames;
}
