package com.marcos.fractalstudio.domain.project;

import java.util.UUID;

public record ProjectId(String value) {

    public ProjectId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Project id is required.");
        }
    }

    public static ProjectId create() {
        return new ProjectId(UUID.randomUUID().toString());
    }
}
