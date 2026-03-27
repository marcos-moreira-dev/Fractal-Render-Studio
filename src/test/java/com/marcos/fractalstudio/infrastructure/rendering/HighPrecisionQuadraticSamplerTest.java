package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.domain.fractal.BurningShipFormula;
import com.marcos.fractalstudio.domain.fractal.CelticMandelbrotFormula;
import com.marcos.fractalstudio.domain.fractal.MandelbrotFormula;
import com.marcos.fractalstudio.domain.fractal.TricornFormula;
import com.marcos.fractalstudio.domain.render.EscapeParameters;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class HighPrecisionQuadraticSamplerTest {

    @Test
    void supportsAllQuadraticFormulasExposedByTheInspector() {
        assertTrue(HighPrecisionQuadraticSampler.supports(new MandelbrotFormula()));
        assertTrue(HighPrecisionQuadraticSampler.supports(new BurningShipFormula()));
        assertTrue(HighPrecisionQuadraticSampler.supports(new TricornFormula()));
        assertTrue(HighPrecisionQuadraticSampler.supports(new CelticMandelbrotFormula()));
    }

    @Test
    void samplesBurningShipWithHighPrecisionPath() {
        double sample = HighPrecisionQuadraticSampler.sample(
                new BurningShipFormula(),
                new BigDecimal("-1.75"),
                new BigDecimal("-0.03"),
                new EscapeParameters(255, 2.0),
                MathContext.DECIMAL128,
                iteration -> {
                }
        );

        assertTrue(sample >= 0.0 && sample <= 1.0);
    }
}
