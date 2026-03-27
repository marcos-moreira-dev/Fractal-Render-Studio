package com.marcos.fractalstudio.domain.project;

import com.marcos.fractalstudio.domain.render.RenderPreset;

public record ProjectSettings(
        double defaultFramesPerSecond,
        int defaultRenderFrameCount,
        double keyframeStepSeconds,
        RenderPreset defaultRenderPreset
) {

    public ProjectSettings {
        if (defaultFramesPerSecond <= 0.0) {
            throw new IllegalArgumentException("Default frames per second must be greater than zero.");
        }
        if (defaultRenderFrameCount <= 0) {
            throw new IllegalArgumentException("Default render frame count must be greater than zero.");
        }
        if (keyframeStepSeconds <= 0.0) {
            throw new IllegalArgumentException("Keyframe step seconds must be greater than zero.");
        }
        if (defaultRenderPreset == null) {
            throw new IllegalArgumentException("Default render preset is required.");
        }
    }
}
