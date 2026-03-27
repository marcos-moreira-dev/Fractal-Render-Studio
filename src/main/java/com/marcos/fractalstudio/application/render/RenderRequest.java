package com.marcos.fractalstudio.application.render;

import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.RenderPreset;

import java.nio.file.Path;

public record RenderRequest(
        Project project,
        CameraState fallbackCamera,
        String jobName,
        int totalFrames,
        double framesPerSecond,
        Path outputDirectory,
        RenderPreset renderPreset
) {

    public RenderRequest {
        if (project == null || fallbackCamera == null) {
            throw new IllegalArgumentException("Render request requires project and fallback camera.");
        }
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Render request requires a job name.");
        }
        if (totalFrames <= 0 || framesPerSecond <= 0.0 || outputDirectory == null || renderPreset == null) {
            throw new IllegalArgumentException("Render request requires positive frame count and fps.");
        }
    }
}
