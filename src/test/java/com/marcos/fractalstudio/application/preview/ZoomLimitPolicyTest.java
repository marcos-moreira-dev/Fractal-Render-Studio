package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.domain.fractal.BurningShipFormula;
import com.marcos.fractalstudio.domain.fractal.MandelbrotFormula;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ZoomLimitPolicyTest {

    @Test
    void blocksZoomInWhenNonHighPrecisionFormulaHitsItsLimit() {
        ZoomLimitPolicy.ZoomLimitDecision decision = ZoomLimitPolicy.evaluate(
                new BurningShipFormula(),
                new BigDecimal("1000000000000"),
                1.18d
        );

        assertTrue(decision.blocked());
    }

    @Test
    void keepsAllowingMandelbrotFarBeyondStandardFormulaLimit() {
        ZoomLimitPolicy.ZoomLimitDecision decision = ZoomLimitPolicy.evaluate(
                new MandelbrotFormula(),
                new BigDecimal("1000000000000"),
                1.18d
        );

        assertFalse(decision.blocked());
    }
}
