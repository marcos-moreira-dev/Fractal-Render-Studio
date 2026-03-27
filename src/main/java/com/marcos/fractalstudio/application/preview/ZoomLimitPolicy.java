package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.domain.fractal.FractalFormula;
import com.marcos.fractalstudio.domain.fractal.MandelbrotFormula;

import java.math.BigDecimal;

/**
 * Guards zoom-in requests when the current formula/precision path is no longer
 * expected to produce meaningful detail.
 */
public final class ZoomLimitPolicy {

    private static final BigDecimal MAX_MANDELBROT_ZOOM = new BigDecimal("1000000000000000000");
    private static final BigDecimal MAX_STANDARD_ZOOM = new BigDecimal("1000000000000");

    private ZoomLimitPolicy() {
    }

    /**
     * Evaluates whether a zoom change should be applied for the current formula.
     *
     * @param fractalFormula active fractal formula
     * @param currentZoom current zoom level
     * @param factor requested multiplicative zoom factor
     * @return decision containing the target zoom and whether the request should be blocked
     */
    public static ZoomLimitDecision evaluate(FractalFormula fractalFormula, BigDecimal currentZoom, double factor) {
        if (factor <= 1.0d) {
            return new ZoomLimitDecision(currentZoom.multiply(BigDecimal.valueOf(factor)), false);
        }
        BigDecimal maxZoom = fractalFormula instanceof MandelbrotFormula ? MAX_MANDELBROT_ZOOM : MAX_STANDARD_ZOOM;
        BigDecimal requestedZoom = currentZoom.multiply(BigDecimal.valueOf(factor));
        if (requestedZoom.compareTo(maxZoom) > 0) {
            return new ZoomLimitDecision(currentZoom, true);
        }
        return new ZoomLimitDecision(requestedZoom, false);
    }

    /**
     * Returns the practical maximum zoom currently supported for the supplied formula.
     *
     * @param fractalFormula active fractal formula
     * @return upper zoom bound used by the desktop interaction layer
     */
    public static BigDecimal maxZoomFor(FractalFormula fractalFormula) {
        return fractalFormula instanceof MandelbrotFormula ? MAX_MANDELBROT_ZOOM : MAX_STANDARD_ZOOM;
    }

    /**
     * Result of evaluating a zoom request.
     *
     * @param targetZoom zoom that should be applied when the request is accepted
     * @param blocked whether the request must be rejected
     */
    public record ZoomLimitDecision(BigDecimal targetZoom, boolean blocked) {
    }
}
