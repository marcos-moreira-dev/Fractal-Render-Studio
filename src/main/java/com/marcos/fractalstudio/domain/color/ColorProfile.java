package com.marcos.fractalstudio.domain.color;

public record ColorProfile(String name, Palette palette) {

    public ColorProfile {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Color profile name is required.");
        }
        if (palette == null) {
            throw new IllegalArgumentException("Palette is required.");
        }
    }
}
