package com.marcos.fractalstudio.domain.camera;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/**
 * Immutable fractal-plane coordinate that preserves more precision than {@code double}.
 */
public final class FractalCoordinate {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private final BigDecimal x;
    private final BigDecimal y;

    public FractalCoordinate(double x, double y) {
        this(BigDecimal.valueOf(x), BigDecimal.valueOf(y));
    }

    public FractalCoordinate(String x, String y) {
        this(new BigDecimal(x), new BigDecimal(y));
    }

    public FractalCoordinate(BigDecimal x, BigDecimal y) {
        this.x = Objects.requireNonNull(x, "Coordinate x is required.");
        this.y = Objects.requireNonNull(y, "Coordinate y is required.");
    }

    public double x() {
        return x.doubleValue();
    }

    public double y() {
        return y.doubleValue();
    }

    public BigDecimal xDecimal() {
        return x;
    }

    public BigDecimal yDecimal() {
        return y;
    }

    public String xPlainString() {
        return x.stripTrailingZeros().toPlainString();
    }

    public String yPlainString() {
        return y.stripTrailingZeros().toPlainString();
    }

    public FractalCoordinate add(double deltaX, double deltaY) {
        return add(BigDecimal.valueOf(deltaX), BigDecimal.valueOf(deltaY));
    }

    public FractalCoordinate add(BigDecimal deltaX, BigDecimal deltaY) {
        return new FractalCoordinate(
                x.add(deltaX, MATH_CONTEXT),
                y.add(deltaY, MATH_CONTEXT)
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FractalCoordinate that)) {
            return false;
        }
        return x.compareTo(that.x) == 0 && y.compareTo(that.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x.stripTrailingZeros(), y.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "FractalCoordinate[x=" + xPlainString() + ", y=" + yPlainString() + "]";
    }
}
