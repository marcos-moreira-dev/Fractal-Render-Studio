package com.marcos.fractalstudio.domain.fractal;

public final class FractalFormulaFactory {

    private FractalFormulaFactory() {
    }

    public static FractalFormula createDefault() {
        return new MandelbrotFormula();
    }

    public static FractalFormula create(FractalFormulaType formulaType) {
        return switch (formulaType) {
            case MANDELBROT -> new MandelbrotFormula();
            case BURNING_SHIP -> new BurningShipFormula();
            case TRICORN -> new TricornFormula();
            case CELTIC_MANDELBROT -> new CelticMandelbrotFormula();
        };
    }

    public static FractalFormulaType resolveType(FractalFormula fractalFormula) {
        if (fractalFormula instanceof MandelbrotFormula) {
            return FractalFormulaType.MANDELBROT;
        }
        if (fractalFormula instanceof BurningShipFormula) {
            return FractalFormulaType.BURNING_SHIP;
        }
        if (fractalFormula instanceof TricornFormula) {
            return FractalFormulaType.TRICORN;
        }
        if (fractalFormula instanceof CelticMandelbrotFormula) {
            return FractalFormulaType.CELTIC_MANDELBROT;
        }
        throw new IllegalArgumentException("Unsupported fractal formula: " + fractalFormula.getClass().getName());
    }
}
