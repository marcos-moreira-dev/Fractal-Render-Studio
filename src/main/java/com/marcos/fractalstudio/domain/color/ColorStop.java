package com.marcos.fractalstudio.domain.color;

public record ColorStop(double position, RgbColor color) {

    public ColorStop {
        if (position < 0.0 || position > 1.0) {
            throw new IllegalArgumentException("Color stop position must be between 0 and 1.");
        }
        if (color == null) {
            throw new IllegalArgumentException("Color stop color is required.");
        }
    }
}
