package com.marcos.fractalstudio.infrastructure.rendering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class AdaptiveEscapeBudgetTest {

    @Test
    void increasesPreviewIterationsAsZoomGrows() {
        int baseIterations = AdaptiveEscapeBudget.previewIterations(128, 1.0);
        int deepIterations = AdaptiveEscapeBudget.previewIterations(128, 4096.0);

        assertTrue(deepIterations > baseIterations);
    }

    @Test
    void increasesFinalIterationsAsZoomGrows() {
        int baseIterations = AdaptiveEscapeBudget.finalIterations(128, 1.0);
        int deepIterations = AdaptiveEscapeBudget.finalIterations(128, 4096.0);

        assertTrue(deepIterations > baseIterations);
    }
}
