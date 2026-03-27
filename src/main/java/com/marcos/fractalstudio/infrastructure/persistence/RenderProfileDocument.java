package com.marcos.fractalstudio.infrastructure.persistence;

public record RenderProfileDocument(
        String name,
        int width,
        int height,
        int maxIterations,
        double escapeRadius,
        String quality
) {
}
