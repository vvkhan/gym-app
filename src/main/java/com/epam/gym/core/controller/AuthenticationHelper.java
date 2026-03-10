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

    public void authenticate(HttpServletRequest request, String username) {
        String[] credentials = CredentialsExtractor.extractCredentials(request);
        if (!credentials[0].equals(username)) {
            throw new SecurityException("Access denied");
        }
        boolean ok = facade.authenticateTrainee(credentials[0], credentials[1])
                || facade.authenticateTrainer(credentials[0], credentials[1]);
        if (!ok) {
            throw new SecurityException("Invalid username or password");
        }
    }

    public void authenticateTrainee(HttpServletRequest request, String username) {
        String[] credentials = CredentialsExtractor.extractCredentials(request);
        if (!credentials[0].equals(username)) {
            throw new SecurityException("Access denied");
        }
        if (!facade.authenticateTrainee(credentials[0], credentials[1])) {
            throw new SecurityException("Invalid username or password");
        }
    }
}
