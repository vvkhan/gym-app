package com.epam.gym.core.exception;

/**
 * Thrown when requested entity is not found in the storage
 */
public class EntityNotFoundException extends GymCrmException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityNotFoundException(String entityType, Long id) {
        super(String.format("%s with id %d not found", entityType, id));
    }

    public EntityNotFoundException(String entityType, String username) {
        super(String.format("%s with username '%s' not found", entityType, username));
    }
}
