package com.marcos.fractalstudio.domain.render;

public record FrameIndex(int value) {

    public FrameIndex {
        if (value < 0) {
            throw new IllegalArgumentException("Frame index must be zero or positive.");
        }
    }
}
