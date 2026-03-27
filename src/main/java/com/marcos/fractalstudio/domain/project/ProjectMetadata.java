package com.marcos.fractalstudio.domain.project;

import java.time.Instant;
import java.util.Objects;

public record ProjectMetadata(
        Instant createdAt,
        Instant updatedAt,
        String description
) {

    public ProjectMetadata {
        Objects.requireNonNull(createdAt, "Project createdAt is required.");
        Objects.requireNonNull(updatedAt, "Project updatedAt is required.");
        description = description == null ? "" : description;
    }

    public static ProjectMetadata create(String description) {
        Instant now = Instant.now();
        return new ProjectMetadata(now, now, description);
    }

    public ProjectMetadata touch() {
        return new ProjectMetadata(createdAt, Instant.now(), description);
    }

    public ProjectMetadata withDescription(String newDescription) {
        return new ProjectMetadata(createdAt, Instant.now(), newDescription == null ? "" : newDescription);
    }
}
