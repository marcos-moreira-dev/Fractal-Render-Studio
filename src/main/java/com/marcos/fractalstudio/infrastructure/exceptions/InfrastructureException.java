package com.marcos.fractalstudio.infrastructure.exceptions;

import java.io.IOException;

/**
 * Base checked exception for technical failures produced by adapters and external resources.
 *
 * <p>Infrastructure exceptions represent errors that come from filesystems, codecs, serialization,
 * operating-system services or third-party libraries. They are checked so callers are forced to either
 * translate them or surface them explicitly.
 */
public class InfrastructureException extends IOException {

    /**
     * Creates an infrastructure failure with a technical message.
     *
     * @param message explanation of the technical problem
     */
    public InfrastructureException(String message) {
        super(message);
    }

    /**
     * Creates an infrastructure failure with its original cause.
     *
     * @param message explanation of the technical problem
     * @param cause original low-level cause
     */
    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
