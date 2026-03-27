package com.marcos.fractalstudio.domain.project;

import java.util.UUID;

public record ProjectBookmarkId(String value) {

    public ProjectBookmarkId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Project bookmark id is required.");
        }
    }

    public static ProjectBookmarkId create() {
        return new ProjectBookmarkId(UUID.randomUUID().toString());
    }
}
