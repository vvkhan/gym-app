package com.epam.gym.core.controller;

import com.epam.gym.core.facade.GymFacade;
import com.epam.gym.core.util.CredentialsExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHelper {

    private final GymFacade facade;

    public AuthenticationHelper(GymFacade facade) {
        this.facade = facade;
    }

    public void authenticateOwner(HttpServletRequest request, String username) {
        String[] credentials = extractAndAuthenticate(request);
        if (!credentials[0].equals(username)) {
            throw new SecurityException("Access denied");
        }
    }

    public void authenticateAny(HttpServletRequest request) {
        extractAndAuthenticate(request);
    }

    // Used only for POST /api/trainings in TrainingController assuming only trainee can add trainings.
    // Keeping the method for explicit type guard.
    // Otherwise, if trainer also can add trainings, can go with authenticateOwner.
    public void authenticateTrainee(HttpServletRequest request, String username) {
        String[] credentials = extractAndAuthenticate(request);
        if (!credentials[0].equals(username)) {
            throw new SecurityException("Access denied");
        }
        if (!facade.isTrainee(credentials[0])) {
            throw new SecurityException("Access denied");
        }
    }

    private String[] extractAndAuthenticate(HttpServletRequest request) {
        String[] credentials = CredentialsExtractor.extractCredentials(request);
        if (!facade.authenticateUser(credentials[0], credentials[1])) {
            throw new SecurityException("Invalid username or password");
        }
        return credentials;
    }
}
