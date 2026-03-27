package com.marcos.fractalstudio.presentation.dialogs;

public record RenderSettingsDialogResult(
        String renderName,
        double durationSeconds,
        double framesPerSecond,
        String baseDirectory
) {
}
