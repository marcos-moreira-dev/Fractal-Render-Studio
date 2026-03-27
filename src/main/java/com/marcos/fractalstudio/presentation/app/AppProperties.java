package com.marcos.fractalstudio.presentation.app;

/**
 * Immutable runtime settings loaded from {@code app.properties}.
 */
public record AppProperties(
        String applicationTitle,
        double initialWidth,
        double initialHeight,
        String storageRoot,
        int previewThreads,
        int renderThreads
) {
}
