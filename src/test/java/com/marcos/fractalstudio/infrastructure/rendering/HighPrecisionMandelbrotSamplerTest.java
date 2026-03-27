package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.domain.fractal.MandelbrotFormula;
import com.marcos.fractalstudio.domain.render.EscapeParameters;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class HighPrecisionMandelbrotSamplerTest {

    @Test
    void supportsCooperativeCancellationDuringDeepZoomSampling() {
        assertThrows(
                CancellationException.class,
                () -> HighPrecisionMandelbrotSampler.sample(
                        new MandelbrotFormula(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new EscapeParameters(255, 2.0),
                        MathContext.DECIMAL128,
                        iteration -> {
                            if (iteration >= 4) {
                                throw new CancellationException("cancelled");
                            }
                        }
                )
        );
    }
}
