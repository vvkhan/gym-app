package com.epam.gym.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "newPassword")
public class ChangePasswordRequest {

    @NotBlank
    private String newPassword;
}
