package com.marcos.fractalstudio.infrastructure.exceptions;

/**
 * Signals that a project snapshot could not be saved to or loaded from persistent storage.
 */
public final class ProjectPersistenceException extends InfrastructureException {

    /**
     * Creates a persistence failure for project JSON operations.
     *
     * @param message explanation of the persistence problem
     * @param cause original filesystem or serialization cause
     */
    public ProjectPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
