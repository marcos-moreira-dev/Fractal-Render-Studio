package com.marcos.fractalstudio.infrastructure.persistence;

public record ProjectSettingsDocument(
        double defaultFramesPerSecond,
        int defaultRenderFrameCount,
        double keyframeStepSeconds,
        String defaultRenderPreset
) {
}
