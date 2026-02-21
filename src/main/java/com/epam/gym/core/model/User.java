package com.epam.gym.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Abstract class for all users types
 */
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    @ToString.Exclude
    private String password;
    private Boolean isActive;
}
