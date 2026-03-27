package com.marcos.fractalstudio.infrastructure.persistence;

public record ProjectMetadataDocument(
        String createdAt,
        String updatedAt,
        String description
) {
}
