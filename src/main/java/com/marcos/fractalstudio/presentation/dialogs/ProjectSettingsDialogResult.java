package com.marcos.fractalstudio.presentation.dialogs;

public record ProjectSettingsDialogResult(
        String projectName,
        String description,
        double defaultFramesPerSecond,
        double defaultDurationSeconds,
        double keyframeStepSeconds,
        String defaultRenderPreset
) {
}
