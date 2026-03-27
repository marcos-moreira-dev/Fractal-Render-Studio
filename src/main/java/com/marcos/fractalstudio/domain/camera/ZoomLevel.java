package com.marcos.fractalstudio.domain.camera;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/**
 * Immutable zoom factor that preserves more precision than {@code double}.
 */
public final class ZoomLevel {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private final BigDecimal value;

    public ZoomLevel(double value) {
        this(BigDecimal.valueOf(value));
    }

    public ZoomLevel(String value) {
        this(new BigDecimal(value));
    }

    public ZoomLevel(BigDecimal value) {
        this.value = Objects.requireNonNull(value, "Zoom level is required.");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("Zoom level must be greater than zero.");
        }
    }

    public double value() {
        return value.doubleValue();
    }

    public BigDecimal valueDecimal() {
        return value;
    }

    public String plainString() {
        return value.stripTrailingZeros().toPlainString();
    }

    public ZoomLevel multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    public ZoomLevel multiply(BigDecimal factor) {
        return new ZoomLevel(value.multiply(factor, MATH_CONTEXT));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ZoomLevel that)) {
            return false;
        }
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "ZoomLevel[value=" + plainString() + "]";
    }
}
