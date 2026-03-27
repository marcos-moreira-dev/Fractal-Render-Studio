package com.marcos.fractalstudio.infrastructure.persistence;

public record KeyframeDocument(
        String id,
        double seconds,
        String centerX,
        String centerY,
        String zoom,
        String label
) {
}
