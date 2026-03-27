package com.marcos.fractalstudio.domain.fractal;

public final class TricornFormula extends AbstractEscapeTimeFormula {

    @Override
    public String name() {
        return "Tricorn";
    }

    @Override
    protected IterationState iterate(double zx, double zy, double x, double y) {
        return new IterationState(
                (zx * zx) - (zy * zy) + x,
                (-2.0 * zx * zy) + y
        );
    }
}
