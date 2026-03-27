package com.marcos.fractalstudio.domain.project;

import com.marcos.fractalstudio.domain.camera.CameraState;

import java.util.Objects;

/**
 * Named mathematical bookmark for returning to a fractal location without storing rasterized frames.
 */
public record ProjectBookmark(
        ProjectBookmarkId id,
        String label,
        CameraState cameraState
) {

    public ProjectBookmark {
        Objects.requireNonNull(id, "Project bookmark id is required.");
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Project bookmark label is required.");
        }
        Objects.requireNonNull(cameraState, "Project bookmark camera state is required.");
    }
}
