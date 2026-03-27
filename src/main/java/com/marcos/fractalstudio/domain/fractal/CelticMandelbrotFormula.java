package com.marcos.fractalstudio.domain.fractal;

public final class CelticMandelbrotFormula extends AbstractEscapeTimeFormula {

    @Override
    public String name() {
        return "Celtic Mandelbrot";
    }

    @Override
    protected IterationState iterate(double zx, double zy, double x, double y) {
        return new IterationState(
                Math.abs((zx * zx) - (zy * zy)) + x,
                (2.0 * zx * zy) + y
        );
    }
}
