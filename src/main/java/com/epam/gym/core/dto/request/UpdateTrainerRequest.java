package com.epam.gym.core.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UpdateTrainerRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Schema(description = "Read-only: ignored by the server, specialization cannot be changed")
    private UUID specializationId;

    @NotNull
    private Boolean isActive;
}
