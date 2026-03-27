package com.marcos.fractalstudio.domain.render;

public record RenderProfile(String name, Resolution resolution, EscapeParameters escapeParameters, RenderQuality quality) {

    public RenderProfile {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Render profile name is required.");
        }
        if (resolution == null) {
            throw new IllegalArgumentException("Resolution is required.");
        }
        if (escapeParameters == null) {
            throw new IllegalArgumentException("Escape parameters are required.");
        }
        if (quality == null) {
            throw new IllegalArgumentException("Render quality is required.");
        }
    }
}
