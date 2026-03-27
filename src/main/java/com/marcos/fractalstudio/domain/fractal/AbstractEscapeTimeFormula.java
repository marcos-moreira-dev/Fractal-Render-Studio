package com.marcos.fractalstudio.domain.fractal;

import com.marcos.fractalstudio.domain.render.EscapeParameters;

/**
 * Shared escape-time implementation for the quadratic fractal family.
 */
abstract class AbstractEscapeTimeFormula implements FractalFormula {

    @Override
    public final double sample(
            double x,
            double y,
            EscapeParameters escapeParameters,
            FractalIterationMonitor iterationMonitor
    ) {
        double zx = 0.0;
        double zy = 0.0;
        double escapeRadiusSquared = escapeParameters.escapeRadius() * escapeParameters.escapeRadius();
        int iteration = 0;

        while ((zx * zx) + (zy * zy) <= escapeRadiusSquared && iteration < escapeParameters.maxIterations()) {
            IterationState nextState = iterate(zx, zy, x, y);
            zx = nextState.real();
            zy = nextState.imaginary();
            iteration++;
            iterationMonitor.onIteration(iteration);
        }

        if (iteration >= escapeParameters.maxIterations()) {
            return 0.0;
        }

        double modulusSquared = (zx * zx) + (zy * zy);
        double smooth = iteration + 1.0 - (Math.log(Math.log(Math.max(modulusSquared, 1.0000001))) / Math.log(2.0));
        return Math.min(1.0, Math.max(0.0, smooth / escapeParameters.maxIterations()));
    }

    protected abstract IterationState iterate(double zx, double zy, double x, double y);

    protected record IterationState(double real, double imaginary) {
    }
}
