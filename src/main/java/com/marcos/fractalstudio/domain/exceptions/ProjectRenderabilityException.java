package com.marcos.fractalstudio.domain.exceptions;

/**
 * Signals that a project cannot be transformed into a coherent render plan.
 *
 * <p>This exception is raised when the project violates render-specific requirements such as minimum
 * resolution, sufficient iteration budget or a timeline that is not valid for animation.
 */
public final class ProjectRenderabilityException extends DomainException {

    /**
     * Creates a renderability failure with a precise business message.
     *
     * @param message explanation of why the project is not renderable
     */
    public ProjectRenderabilityException(String message) {
        super(message);
    }
}
