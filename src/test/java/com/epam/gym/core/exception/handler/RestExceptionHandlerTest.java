package com.epam.gym.core.exception.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    @Test
    void handleAccessDenied_returns403() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleAccessDenied(new AccessDeniedException("Access is denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().get("message"));
    }

    @Test
    void handleSecurity_returns401WithGenericMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleSecurity(new SecurityException("bad credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication failed", response.getBody().get("message"));
    }

    @Test
    void handleNotFound_returns404WithExceptionMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(new NoSuchElementException("Trainee not found: alice"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Trainee not found: alice", response.getBody().get("message"));
    }

    @Test
    void handleIllegalState_returns409() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalState(new IllegalStateException("Trainee is already active: alice"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Trainee is already active: alice", response.getBody().get("message"));
    }

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Invalid input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().get("message"));
    }

    @Test
    void handleMissingParam_returns400WithParameterName() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleMissingParam(new MissingServletRequestParameterException("password", "String"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String message = (String) response.getBody().get("message");
        assert message.contains("password");
    }

    @Test
    void handleValidation_returns400WithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "firstName", "must not be blank"),
                new FieldError("obj", "lastName", "must not be blank")
        ));

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String message = (String) response.getBody().get("message");
        assertNotNull(message);
        assert message.contains("firstName");
        assert message.contains("lastName");
    }

    @Test
    void handleNoHandler_returns404() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/api/unknown", null);

        ResponseEntity<Map<String, Object>> response = handler.handleNoHandler(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String message = (String) response.getBody().get("message");
        assert message.contains("/api/unknown");
    }

    @Test
    void handleAll_returns500WithGenericMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleAll(new RuntimeException("Something went wrong internally"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }

    @Test
    void errorResponse_alwaysContainsTimestampAndStatus() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleSecurity(new SecurityException("test error response"));

        assertNotNull(response.getBody().get("timestamp"));
        assertNotNull(response.getBody().get("status"));
        assertNotNull(response.getBody().get("message"));
    }
}
