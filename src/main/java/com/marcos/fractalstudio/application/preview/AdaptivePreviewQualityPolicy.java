package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.infrastructure.rendering.AdaptiveEscapeBudget;
import com.marcos.fractalstudio.domain.render.Resolution;

/**
 * Chooses the execution strategy for preview rendering based on interaction
 * intent, viewport size and current zoom depth.
 *
 * <p>The policy exists to protect responsiveness. A preview that is visually
 * useful but cheap to compute is often better for interaction than an exact
 * preview that arrives too late to help navigation. Manual refinement requests
 * can still demand full resolution and higher fidelity.
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

    /**
     * Computes the downscale factor applied to automatic previews.
     *
     * <p>The thresholds encode a practical latency policy: as zoom and viewport
     * cost grow, the system sacrifices preview density before sacrificing
     * interactivity altogether.
     */
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

    /**
     * Caps iteration count for fast interaction mode.
     *
     * <p>This method intentionally keeps a floor so previews do not collapse
     * into uselessly coarse images, while still preventing deep zoom from
     * monopolizing the preview executor.
     */
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
         * Returns a compact status label suitable for overlays and inspector
         * panels.
         */
        public String statusLabel() {
            return modeLabel + " " + Math.round(scale * 100.0) + "%";
        }

        /**
         * Returns the internal render-profile name used by the preview pipeline.
         *
         * <p>This name is not just decorative. It makes logs, metrics and
         * debugging output more explicit about which kind of preview path is
         * executing.
         */
        public String profileName() {
            if (precise) {
                return "Interactive Precise Preview";
            }
            return highPrecisionEnabled ? "Interactive Fast Preview High Precision" : "Interactive Fast Preview";
        }
    }
}
