package com.marcos.fractalstudio.domain.exceptions;

/**
 * Base runtime exception for business and mathematical invariants in the domain layer.
 *
 * <p>Domain exceptions model failures that are part of the meaning of the software rather than
 * operating-system or library faults. They signal that a project, timeline or camera state violates
 * rules that the product considers semantically invalid.
 */
public class DomainException extends RuntimeException {

    /**
     * Creates a domain exception with a user-meaningful message.
     *
     * @param message explanation of the violated invariant
     */
    public DomainException(String message) {
        super(message);
    }

    /**
     * Creates a domain exception with additional underlying context.
     *
     * @param message explanation of the violated invariant
     * @param cause original cause that triggered the domain failure
     */
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
