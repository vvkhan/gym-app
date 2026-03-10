package com.epam.gym.core.controller;

import com.epam.gym.core.facade.GymFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationHelperTest {

    @Mock
    private GymFacade facade;

    @InjectMocks
    private AuthenticationHelper authHelper;

    private MockHttpServletRequest requestWithAuth(String username, String password) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        request.addHeader("Authorization", "Basic " + encoded);
        return request;
    }

    private MockHttpServletRequest requestWithoutAuth() {
        return new MockHttpServletRequest();
    }

    // authenticate (any user)

    @Test
    void authenticate_validTraineeCredentialsNoException() {
        when(facade.authenticateTrainee("alice", "pass")).thenReturn(true);

        assertDoesNotThrow(() -> authHelper.authenticate(requestWithAuth("alice", "pass"), "alice"));
    }

    @Test
    void authenticate_validTrainerCredentialsNoException() {
        when(facade.authenticateTrainee("john", "pass")).thenReturn(false);
        when(facade.authenticateTrainer("john", "pass")).thenReturn(true);

        assertDoesNotThrow(() -> authHelper.authenticate(requestWithAuth("john", "pass"), "john"));
    }

    @Test
    void authenticate_usernameMismatchThrowsAccessDenied() {
        assertThrows(SecurityException.class,
                () -> authHelper.authenticate(requestWithAuth("bob", "pass"), "alice"));
    }

    @Test
    void authenticate_invalidCredentialsThrowsUnauthorized() {
        when(facade.authenticateTrainee("alice", "wrong")).thenReturn(false);
        when(facade.authenticateTrainer("alice", "wrong")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> authHelper.authenticate(requestWithAuth("alice", "wrong"), "alice"));
    }

    @Test
    void authenticate_missingHeaderThrowsSecurityException() {
        assertThrows(SecurityException.class,
                () -> authHelper.authenticate(requestWithoutAuth(), "alice"));
    }

    // authenticateTrainee (trainee only)

    @Test
    void authenticateTrainee_validCredentialsNoException() {
        when(facade.authenticateTrainee("alice", "pass")).thenReturn(true);

        assertDoesNotThrow(() -> authHelper.authenticateTrainee(requestWithAuth("alice", "pass"), "alice"));
    }

    @Test
    void authenticateTrainee_trainerCredentialsThrowsUnauthorized() {
        when(facade.authenticateTrainee("john", "pass")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> authHelper.authenticateTrainee(requestWithAuth("john", "pass"), "john"));
    }

    @Test
    void authenticateTrainee_usernameMismatchThrowsAccessDenied() {
        assertThrows(SecurityException.class,
                () -> authHelper.authenticateTrainee(requestWithAuth("bob", "pass"), "alice"));
    }

    @Test
    void authenticateTrainee_missingHeaderThrowsSecurityException() {
        assertThrows(SecurityException.class,
                () -> authHelper.authenticateTrainee(requestWithoutAuth(), "alice"));
    }
}
