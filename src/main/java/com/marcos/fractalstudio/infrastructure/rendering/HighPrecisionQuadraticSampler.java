package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.domain.fractal.BurningShipFormula;
import com.marcos.fractalstudio.domain.fractal.CelticMandelbrotFormula;
import com.marcos.fractalstudio.domain.fractal.FractalFormula;
import com.marcos.fractalstudio.domain.fractal.FractalIterationMonitor;
import com.marcos.fractalstudio.domain.fractal.MandelbrotFormula;
import com.marcos.fractalstudio.domain.fractal.TricornFormula;
import com.marcos.fractalstudio.domain.render.EscapeParameters;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * High-precision sampler for the quadratic escape-time fractal family supported
 * by the desktop renderer.
 */
final class HighPrecisionQuadraticSampler {

    private static final BigDecimal TWO = BigDecimal.valueOf(2L);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4L);

    private HighPrecisionQuadraticSampler() {
    }

    static boolean supports(FractalFormula fractalFormula) {
        return fractalFormula instanceof MandelbrotFormula
                || fractalFormula instanceof BurningShipFormula
                || fractalFormula instanceof TricornFormula
                || fractalFormula instanceof CelticMandelbrotFormula;
    }

    static boolean shouldUseHighPrecision(BigDecimal zoom) {
        return zoom.compareTo(BigDecimal.valueOf(1_000_000L)) >= 0;
    }

    static MathContext mathContextForZoom(BigDecimal zoom) {
        int integerDigits = Math.max(1, zoom.precision() - zoom.scale());
        return new MathContext(Math.max(34, integerDigits + 24), RoundingMode.HALF_EVEN);
    }

    static double sample(
            FractalFormula fractalFormula,
            BigDecimal x,
            BigDecimal y,
            EscapeParameters escapeParameters,
            MathContext mathContext,
            FractalIterationMonitor iterationMonitor
    ) {
        BigDecimal zx = BigDecimal.ZERO;
        BigDecimal zy = BigDecimal.ZERO;
        BigDecimal escapeRadius = BigDecimal.valueOf(escapeParameters.escapeRadius());
        BigDecimal escapeRadiusSquared = escapeRadius.multiply(escapeRadius, mathContext);
        int iteration = 0;

        while (magnitudeSquared(zx, zy, mathContext).compareTo(escapeRadiusSquared) <= 0
                && iteration < escapeParameters.maxIterations()) {
            IterationState nextState = iterate(fractalFormula, zx, zy, x, y, mathContext);
            zx = nextState.real();
            zy = nextState.imaginary();
            iteration++;
            iterationMonitor.onIteration(iteration);
        }

        if (iteration >= escapeParameters.maxIterations()) {
            return 0.0;
        }

        double modulusSquared = magnitudeSquared(zx, zy, mathContext).doubleValue();
        double smooth = iteration + 1.0 - (Math.log(Math.log(Math.max(modulusSquared, 1.0000001))) / Math.log(2.0));
        return Math.min(1.0, Math.max(0.0, smooth / escapeParameters.maxIterations()));
    }

    private static IterationState iterate(
            FractalFormula fractalFormula,
            BigDecimal zx,
            BigDecimal zy,
            BigDecimal x,
            BigDecimal y,
            MathContext mathContext
    ) {
        if (fractalFormula instanceof BurningShipFormula) {
            BigDecimal absZx = zx.abs();
            BigDecimal absZy = zy.abs();
            return new IterationState(
                    absZx.multiply(absZx, mathContext).subtract(absZy.multiply(absZy, mathContext), mathContext).add(x, mathContext),
                    TWO.multiply(absZx, mathContext).multiply(absZy, mathContext).add(y, mathContext)
            );
        }
        if (fractalFormula instanceof TricornFormula) {
            return new IterationState(
                    zx.multiply(zx, mathContext).subtract(zy.multiply(zy, mathContext), mathContext).add(x, mathContext),
                    TWO.multiply(zx, mathContext).multiply(zy, mathContext).negate(mathContext).add(y, mathContext)
            );
        }
        if (fractalFormula instanceof CelticMandelbrotFormula) {
            return new IterationState(
                    zx.multiply(zx, mathContext).subtract(zy.multiply(zy, mathContext), mathContext).abs().add(x, mathContext),
                    TWO.multiply(zx, mathContext).multiply(zy, mathContext).add(y, mathContext)
            );
        }
        return new IterationState(
                zx.multiply(zx, mathContext).subtract(zy.multiply(zy, mathContext), mathContext).add(x, mathContext),
                TWO.multiply(zx, mathContext).multiply(zy, mathContext).add(y, mathContext)
        );
    }

    private static BigDecimal magnitudeSquared(BigDecimal zx, BigDecimal zy, MathContext mathContext) {
        return zx.multiply(zx, mathContext).add(zy.multiply(zy, mathContext), mathContext);
    }

    private record IterationState(BigDecimal real, BigDecimal imaginary) {
    }
}
