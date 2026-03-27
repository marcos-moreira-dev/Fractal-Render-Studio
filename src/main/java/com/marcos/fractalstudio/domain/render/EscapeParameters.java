package com.marcos.fractalstudio.domain.render;

public record EscapeParameters(int maxIterations, double escapeRadius) {

    public EscapeParameters {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Max iterations must be greater than zero.");
        }
        if (escapeRadius <= 0.0) {
            throw new IllegalArgumentException("Escape radius must be greater than zero.");
        }
    }
}
