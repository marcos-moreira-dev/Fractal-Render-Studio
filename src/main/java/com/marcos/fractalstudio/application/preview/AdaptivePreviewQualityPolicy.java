package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.infrastructure.rendering.AdaptiveEscapeBudget;
import com.marcos.fractalstudio.domain.render.Resolution;

/**
 * Chooses an interactive preview resolution based on zoom depth.
 * Manual preview requests stay at full viewport resolution, while auto-preview can downscale.
 */
public final class AdaptivePreviewQualityPolicy {

    private static final int MIN_PREVIEW_SIDE = 64;

    private AdaptivePreviewQualityPolicy() {
    }

    /**
     * Builds the preview strategy for the current viewport and zoom depth.
     *
     * @param viewportWidth viewport width in pixels
     * @param viewportHeight viewport height in pixels
     * @param zoom current camera zoom
     * @param baseMaxIterations project-level iteration budget
     * @param precise whether the caller requests a precise preview instead of an interactive one
     * @return adaptive preview plan with resolution, iteration budget and precision flags
     */
    public static PreviewRequestPlan plan(
            double viewportWidth,
            double viewportHeight,
            double zoom,
            int baseMaxIterations,
            boolean precise
    ) {
        double scale = precise ? 1.0 : scaleFor(zoom, viewportWidth * viewportHeight);
        int width = Math.max(MIN_PREVIEW_SIDE, (int) Math.round(viewportWidth * scale));
        int height = Math.max(MIN_PREVIEW_SIDE, (int) Math.round(viewportHeight * scale));
        int preciseIterations = AdaptiveEscapeBudget.previewIterations(baseMaxIterations, zoom);
        int maxIterations = precise ? preciseIterations : fastIterations(zoom, preciseIterations);
        boolean highPrecisionEnabled = precise;
        return new PreviewRequestPlan(
                new Resolution(width, height),
                precise ? "Preview preciso" : "Preview rapido",
                scale,
                precise,
                maxIterations,
                highPrecisionEnabled
        );
    }

    private static double scaleFor(double zoom, double viewportArea) {
        double zoomScale;
        if (zoom >= 1_000_000_000d) {
            zoomScale = 0.40;
        } else if (zoom >= 100_000_000d) {
            zoomScale = 0.48;
        } else if (zoom >= 25_000_000d) {
            zoomScale = 0.56;
        } else if (zoom >= 1_000_000d) {
            zoomScale = 0.82;
        } else if (zoom >= 10_000d) {
            zoomScale = 0.90;
        } else {
            zoomScale = 1.0;
        }

        if (viewportArea >= 1_600_000d) {
            return Math.min(zoomScale, 0.75);
        }
        if (viewportArea >= 1_000_000d) {
            return Math.min(zoomScale, 0.85);
        }
        return zoomScale;
    }

    private static int fastIterations(double zoom, int preciseIterations) {
        int targetCap;
        if (zoom >= 1_000_000_000d) {
            targetCap = 96;
        } else if (zoom >= 100_000_000d) {
            targetCap = 112;
        } else if (zoom >= 25_000_000d) {
            targetCap = 128;
        } else if (zoom >= 1_000_000d) {
            targetCap = 256;
        } else if (zoom >= 10_000d) {
            targetCap = 320;
        } else {
            targetCap = preciseIterations;
        }
        return Math.max(64, Math.min(preciseIterations, targetCap));
    }

    /**
     * Immutable preview execution plan selected for the current interaction context.
     *
     * @param resolution target preview resolution
     * @param modeLabel human-readable preview mode name
     * @param scale viewport scale applied to the preview
     * @param precise whether the plan represents a precise preview
     * @param maxIterations effective iteration cap for the preview
     * @param highPrecisionEnabled whether deep-precision math should be enabled
     */
    public record PreviewRequestPlan(
            Resolution resolution,
            String modeLabel,
            double scale,
            boolean precise,
            int maxIterations,
            boolean highPrecisionEnabled
    ) {
        /**
         * @return short UI label for overlay and inspector status
         */
        public String statusLabel() {
            return modeLabel + " " + Math.round(scale * 100.0) + "%";
        }

        /**
         * @return render-profile name used internally by the preview pipeline
         */
        public String profileName() {
            if (precise) {
                return "Interactive Precise Preview";
            }
            return highPrecisionEnabled ? "Interactive Fast Preview High Precision" : "Interactive Fast Preview";
        }
    }
}
