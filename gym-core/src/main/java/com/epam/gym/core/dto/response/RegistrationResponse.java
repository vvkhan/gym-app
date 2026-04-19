package com.epam.gym.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(exclude = "password")
public class RegistrationResponse {

    private String username;
    private String password;
}
