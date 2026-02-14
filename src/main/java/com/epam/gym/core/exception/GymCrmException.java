package com.epam.gym.core.exception;

/**
 * Base exception for all Gym system exceptions
 */
public class GymCrmException extends RuntimeException {

    public GymCrmException(String message) {
        super(message);
    }

    public GymCrmException(String message, Throwable cause) {
        super(message, cause);
    }

    public GymCrmException(Throwable cause) {
        super(cause);
    }
}
