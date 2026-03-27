package com.marcos.fractalstudio.application.preview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DeepZoomAdvisorTest {

    @Test
    void marksInteractivePreviewAsProtectedAtExtremeZoom() {
        DeepZoomAdvisory advisory = DeepZoomAdvisor.evaluate(
                500_000_000d,
                192,
                960.0,
                540.0,
                false,
                false,
                new MemoryPressureSnapshot(128L * 1024L * 1024L, 256L * 1024L * 1024L, 1024L * 1024L * 1024L)
        );

        assertEquals("Protegido", advisory.healthLabel());
        assertEquals(false, advisory.showDialog());
    }

    @Test
    void warnsForExpensivePrecisePreview() {
        DeepZoomAdvisory advisory = DeepZoomAdvisor.evaluate(
                500_000_000d,
                255,
                1280.0,
                720.0,
                true,
                true,
                new MemoryPressureSnapshot(256L * 1024L * 1024L, 512L * 1024L * 1024L, 1024L * 1024L * 1024L)
        );

        assertEquals("Zona pesada", advisory.healthLabel());
        assertTrue(advisory.showDialog());
    }

    @Test
    void raisesMemoryHighWhenHeapPressureIsReal() {
        DeepZoomAdvisory advisory = DeepZoomAdvisor.evaluate(
                50_000_000d,
                220,
                1280.0,
                720.0,
                true,
                true,
                new MemoryPressureSnapshot(900L * 1024L * 1024L, 950L * 1024L * 1024L, 1024L * 1024L * 1024L)
        );

        assertEquals("Memoria alta", advisory.healthLabel());
        assertTrue(advisory.showDialog());
    }
}
