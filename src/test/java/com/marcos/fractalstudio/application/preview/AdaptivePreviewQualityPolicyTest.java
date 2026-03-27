package com.marcos.fractalstudio.application.preview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AdaptivePreviewQualityPolicyTest {

    @Test
    void keepsManualPreviewAtFullResolution() {
        AdaptivePreviewQualityPolicy.PreviewRequestPlan previewPlan =
                AdaptivePreviewQualityPolicy.plan(1200.0, 800.0, 250_000_000d, 128, true);

        assertEquals(1200, previewPlan.resolution().width());
        assertEquals(800, previewPlan.resolution().height());
        assertEquals("Preview preciso 100%", previewPlan.statusLabel());
        assertEquals(845, previewPlan.maxIterations());
        assertEquals(true, previewPlan.highPrecisionEnabled());
    }

    @Test
    void downscalesInteractivePreviewAtExtremeZoom() {
        AdaptivePreviewQualityPolicy.PreviewRequestPlan previewPlan =
                AdaptivePreviewQualityPolicy.plan(1200.0, 800.0, 250_000_000d, 128, false);

        assertEquals(576, previewPlan.resolution().width());
        assertEquals(384, previewPlan.resolution().height());
        assertEquals("Preview rapido 48%", previewPlan.statusLabel());
        assertEquals(112, previewPlan.maxIterations());
        assertEquals(false, previewPlan.highPrecisionEnabled());
    }
}
