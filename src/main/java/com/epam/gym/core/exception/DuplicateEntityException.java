package com.epam.gym.core.exception;

/**
 * Thrown when attempting to create entity that already exists
 */
public class DuplicateEntityException extends GymCrmException {

    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateEntityException(String entityType, String identifier) {
        super(String.format("%s with identifier '%s' already exists", entityType, identifier));
    }
}
