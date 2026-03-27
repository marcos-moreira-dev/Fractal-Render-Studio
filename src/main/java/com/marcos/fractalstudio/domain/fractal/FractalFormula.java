package com.marcos.fractalstudio.domain.fractal;

import com.marcos.fractalstudio.domain.render.EscapeParameters;

public interface FractalFormula {

    String name();

    default double sample(double x, double y, EscapeParameters escapeParameters) {
        return sample(x, y, escapeParameters, FractalIterationMonitor.none());
    }

    double sample(double x, double y, EscapeParameters escapeParameters, FractalIterationMonitor iterationMonitor);
}
