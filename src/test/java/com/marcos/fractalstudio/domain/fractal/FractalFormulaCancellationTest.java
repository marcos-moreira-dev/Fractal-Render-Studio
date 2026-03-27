package com.marcos.fractalstudio.domain.fractal;

import com.marcos.fractalstudio.domain.render.EscapeParameters;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class FractalFormulaCancellationTest {

    @Test
    void formulasSupportCooperativeCancellationDuringIteration() {
        EscapeParameters escapeParameters = new EscapeParameters(255, 2.0);
        List<FractalFormula> formulas = List.of(
                new MandelbrotFormula(),
                new BurningShipFormula(),
                new TricornFormula(),
                new CelticMandelbrotFormula()
        );

        for (FractalFormula formula : formulas) {
            assertThrows(
                    CancellationException.class,
                    () -> formula.sample(0.0, 0.0, escapeParameters, iteration -> {
                        if (iteration >= 4) {
                            throw new CancellationException("cancelled");
                        }
                    }),
                    formula.name()
            );
        }
    }
}
