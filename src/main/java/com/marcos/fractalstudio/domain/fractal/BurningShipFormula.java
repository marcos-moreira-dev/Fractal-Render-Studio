package com.marcos.fractalstudio.domain.fractal;

public final class BurningShipFormula extends AbstractEscapeTimeFormula {

    @Override
    public String name() {
        return "Burning Ship";
    }

    @Override
    protected IterationState iterate(double zx, double zy, double x, double y) {
        double absZx = Math.abs(zx);
        double absZy = Math.abs(zy);
        return new IterationState(
                (absZx * absZx) - (absZy * absZy) + x,
                (2.0 * absZx * absZy) + y
        );
    }
}
