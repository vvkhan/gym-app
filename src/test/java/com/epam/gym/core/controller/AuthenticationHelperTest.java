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

    // authenticateOwner (specific user)

    @Test
    void authenticateOwner_validCredentialsNoException() {
        when(facade.authenticateUser("alice", "pass")).thenReturn(true);

        assertDoesNotThrow(() -> authHelper.authenticateOwner(requestWithAuth("alice", "pass"), "alice"));
    }

    @Test
    void authenticateOwner_usernameMismatchThrowsAccessDenied() {
        assertThrows(SecurityException.class,
                () -> authHelper.authenticateOwner(requestWithAuth("bob", "pass"), "alice"));
    }

    @Test
    void authenticateOwner_invalidCredentialsThrowsUnauthorized() {
        when(facade.authenticateUser("alice", "wrong")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> authHelper.authenticateOwner(requestWithAuth("alice", "wrong"), "alice"));
    }

    @Test
    void authenticateOwner_missingHeaderThrowsSecurityException() {
        assertThrows(SecurityException.class,
                () -> authHelper.authenticateOwner(requestWithoutAuth(), "alice"));
    }

    // authenticateAny (any valid user)

    @Test
    void authenticateAny_validCredentialsNoException() {
        when(facade.authenticateUser("alice", "pass")).thenReturn(true);

        assertDoesNotThrow(() -> authHelper.authenticateAny(requestWithAuth("alice", "pass")));
    }

    // authenticateTrainee (trainee only)

    @Test
    void authenticateTrainee_validCredentialsNoException() {
        when(facade.authenticateUser("alice", "pass")).thenReturn(true);
        when(facade.isTrainee("alice")).thenReturn(true);

        assertDoesNotThrow(() -> authHelper.authenticateTrainee(requestWithAuth("alice", "pass"), "alice"));
    }

    @Test
    void authenticateTrainee_trainerCredentialsThrowsAccessDenied() {
        when(facade.authenticateUser("john", "pass")).thenReturn(true);
        when(facade.isTrainee("john")).thenReturn(false);

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
