package com.marcos.fractalstudio.infrastructure.exceptions;

/**
 * Signals that the MP4 encoding pipeline failed while converting rendered frames to video.
 */
public final class VideoEncodingException extends InfrastructureException {

    /**
     * Creates a video-encoding failure.
     *
     * @param message explanation of the encoding problem
     * @param cause original FFmpeg or filesystem cause
     */
    public VideoEncodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
