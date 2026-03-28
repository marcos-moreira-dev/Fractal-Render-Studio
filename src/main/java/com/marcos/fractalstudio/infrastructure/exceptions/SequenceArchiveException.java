package com.marcos.fractalstudio.infrastructure.exceptions;

/**
 * Signals that a frame sequence could not be archived into a distributable container.
 */
public final class SequenceArchiveException extends InfrastructureException {

    /**
     * Creates an archive export failure.
     *
     * @param message explanation of the archive problem
     * @param cause original filesystem or ZIP cause
     */
    public SequenceArchiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
