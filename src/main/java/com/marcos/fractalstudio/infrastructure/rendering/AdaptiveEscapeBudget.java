package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;

public final class AdaptiveEscapeBudget {

    private AdaptiveEscapeBudget() {
    }

    public static EscapeParameters forPreview(FrameDescriptor frameDescriptor) {
        EscapeParameters baseParameters = frameDescriptor.renderProfile().escapeParameters();
        int adaptiveIterations = previewIterations(baseParameters.maxIterations(), frameDescriptor.cameraState().zoomLevel().value());
        return new EscapeParameters(adaptiveIterations, baseParameters.escapeRadius());
    }

    public static EscapeParameters forFinal(FrameDescriptor frameDescriptor) {
        EscapeParameters baseParameters = frameDescriptor.renderProfile().escapeParameters();
        int adaptiveIterations = finalIterations(baseParameters.maxIterations(), frameDescriptor.cameraState().zoomLevel().value());
        return new EscapeParameters(adaptiveIterations, baseParameters.escapeRadius());
    }

    public static int previewIterations(int baseMaxIterations, double zoom) {
        int baseIterations = Math.max(48, baseMaxIterations / 2);
        return scaleIterations(baseIterations, zoom, 28.0, 4096);
    }

    public static int finalIterations(int baseMaxIterations, double zoom) {
        int baseIterations = Math.max(128, baseMaxIterations);
        return scaleIterations(baseIterations, zoom, 40.0, 8192);
    }

    private static int scaleIterations(int baseIterations, double zoom, double growthPerOctave, int cap) {
        double safeZoom = Math.max(1.0, zoom);
        double octaves = Math.log(safeZoom) / Math.log(2.0);
        int scaledIterations = (int) Math.round(baseIterations + (Math.max(0.0, octaves) * growthPerOctave));
        return Math.max(baseIterations, Math.min(cap, scaledIterations));
    }
}
