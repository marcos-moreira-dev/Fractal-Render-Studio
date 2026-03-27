package com.marcos.fractalstudio.infrastructure.batching;

import java.util.UUID;

public record RenderJobId(String value) {

    public static RenderJobId create() {
        return new RenderJobId(UUID.randomUUID().toString());
    }
}
