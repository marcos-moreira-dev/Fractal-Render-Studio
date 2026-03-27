package com.marcos.fractalstudio.domain.timeline;

import java.util.UUID;

public record KeyframeId(String value) {

    public KeyframeId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Keyframe id is required.");
        }
    }

    public static KeyframeId create() {
        return new KeyframeId(UUID.randomUUID().toString());
    }
}
