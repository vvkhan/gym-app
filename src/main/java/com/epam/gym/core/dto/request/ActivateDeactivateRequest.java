package com.epam.gym.core.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivateDeactivateRequest {

    @NotNull
    private Boolean isActive;
}
