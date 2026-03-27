package com.marcos.fractalstudio.application.preview;

/**
 * Non-fatal advisory describing when a deep-zoom preview may become expensive or approach practical limits.
 */
public record DeepZoomAdvisory(
        String headline,
        String message,
        String healthLabel,
        String memoryLabel,
        boolean showDialog
) {
}
