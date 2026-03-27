package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.domain.fractal.FractalIterationMonitor;
import com.marcos.fractalstudio.domain.fractal.FractalFormula;
import com.marcos.fractalstudio.domain.render.EscapeParameters;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Compatibility wrapper around the generalized quadratic high-precision sampler.
 */
final class HighPrecisionMandelbrotSampler {

    private HighPrecisionMandelbrotSampler() {
    }

    static boolean shouldUseHighPrecision(BigDecimal zoom) {
        return HighPrecisionQuadraticSampler.shouldUseHighPrecision(zoom);
    }

    static MathContext mathContextForZoom(BigDecimal zoom) {
        return HighPrecisionQuadraticSampler.mathContextForZoom(zoom);
    }

    static double sample(
            FractalFormula fractalFormula,
            BigDecimal x,
            BigDecimal y,
            EscapeParameters escapeParameters,
            MathContext mathContext,
            FractalIterationMonitor iterationMonitor
    ) {
        return HighPrecisionQuadraticSampler.sample(fractalFormula, x, y, escapeParameters, mathContext, iterationMonitor);
    }
}
