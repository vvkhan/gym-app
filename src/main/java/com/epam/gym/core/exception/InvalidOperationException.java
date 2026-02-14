package com.epam.gym.core.exception;

/**
 * Thrown for invalid operation:
 * deleting a non-existent entity, updating with invalid data etc.
 */
public class InvalidOperationException extends GymCrmException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
