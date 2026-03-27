package com.marcos.fractalstudio.application.preview;

public record RenderedFrame(int width, int height, int[] argbPixels) {

    public RenderedFrame {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Rendered frame dimensions must be positive.");
        }
        if (argbPixels == null || argbPixels.length != width * height) {
            throw new IllegalArgumentException("Rendered frame pixel buffer size is invalid.");
        }
    }
}
