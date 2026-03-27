package com.marcos.fractalstudio.application.render;

import com.marcos.fractalstudio.domain.render.FrameDescriptor;

import java.nio.file.Path;
import java.util.List;

public record RenderPlan(String jobName, Path outputDirectory, double framesPerSecond, List<FrameDescriptor> frameDescriptors) {

    public RenderPlan {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Render plan job name is required.");
        }
        if (outputDirectory == null || frameDescriptors == null || frameDescriptors.isEmpty() || framesPerSecond <= 0.0) {
            throw new IllegalArgumentException("Render plan requires at least one frame.");
        }
    }
}
