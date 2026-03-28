package com.marcos.fractalstudio.domain.exceptions;

/**
 * Signals that a timeline violates ordering or coherence rules required by the animation model.
 *
 * <p>The application expects keyframes to be strictly ordered by time so interpolation remains
 * deterministic. When that invariant is broken, the timeline is semantically invalid even if the
 * underlying data structures are otherwise well-formed.
 */
public final class TimelineConsistencyException extends DomainException {

    /**
     * Creates a timeline consistency failure with a domain-level message.
     *
     * @param message explanation of the violated timeline rule
     */
    public TimelineConsistencyException(String message) {
        super(message);
    }
}
