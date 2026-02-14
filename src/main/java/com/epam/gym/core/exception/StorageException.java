package com.epam.gym.core.exception;

/**
 * Thrown for storage-related error:
 * I/O errors, JSON parsing errors etc.
 */
public class StorageException extends GymCrmException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(Throwable cause) {
        super("Storage operation failed", cause);
    }
}
