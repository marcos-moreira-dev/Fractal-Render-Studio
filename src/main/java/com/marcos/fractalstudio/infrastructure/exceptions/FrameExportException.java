package com.marcos.fractalstudio.infrastructure.exceptions;

/**
 * Signals that a rendered frame could not be serialized to an image file.
 */
public final class FrameExportException extends InfrastructureException {

    /**
     * Creates a frame export failure.
     *
     * @param message explanation of the image export problem
     * @param cause original filesystem or image-encoding cause
     */
    public FrameExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
