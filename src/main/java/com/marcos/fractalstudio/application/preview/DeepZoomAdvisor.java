package com.marcos.fractalstudio.application.preview;

public final class DeepZoomAdvisor {

    private DeepZoomAdvisor() {
    }

    /**
     * Combines preview cost heuristics with real JVM heap pressure to classify
     * the current deep-zoom situation.
     *
     * @param zoom current camera zoom
     * @param maxIterations effective iteration budget for the preview
     * @param viewportWidth viewport width in pixels
     * @param viewportHeight viewport height in pixels
     * @param precise whether the caller requested a precise preview
     * @param highPrecisionEnabled whether deep-precision math is enabled
     * @param memoryPressureSnapshot current JVM memory snapshot
     * @return advisory for overlays, inspector state and optional dialogs
     */
    public static DeepZoomAdvisory evaluate(
            double zoom,
            int maxIterations,
            double viewportWidth,
            double viewportHeight,
            boolean precise,
            boolean highPrecisionEnabled,
            MemoryPressureSnapshot memoryPressureSnapshot
    ) {
        double viewportArea = Math.max(1.0, viewportWidth * viewportHeight);
        double costScore = viewportArea * maxIterations * (highPrecisionEnabled ? 4.0 : 1.0);
        String memoryLabel = memoryPressureSnapshot.compactLabel();
        boolean memoryPressureHigh = memoryPressureSnapshot.usageRatio() >= 0.82d
                || memoryPressureSnapshot.availableBytes() < 256L * 1024L * 1024L;

        if (zoom < 1_000_000d && costScore < 180_000_000d && !memoryPressureHigh) {
            return new DeepZoomAdvisory(
                    "Deep zoom estable",
                    "El nivel actual se mantiene dentro de un coste razonable para preview.",
                    "Estable",
                    memoryLabel,
                    false
            );
        }

        if (zoom < 100_000_000d && costScore < 350_000_000d && !memoryPressureHigh) {
            return new DeepZoomAdvisory(
                    "Deep zoom exigente",
                    "El preview sigue siendo viable, pero el tiempo de calculo ya depende bastante del zoom y las iteraciones.",
                    "Exigente",
                    memoryLabel,
                    false
            );
        }

        if (memoryPressureHigh && !precise) {
            return new DeepZoomAdvisory(
                    "Preview rapido protegido",
                    "La memoria JVM esta entrando en una zona alta de uso. El preview interactivo se mantiene degradado para evitar atascar la sesion.",
                    "Memoria alta",
                    memoryLabel,
                    false
            );
        }

        if (!precise) {
            return new DeepZoomAdvisory(
                    "Preview rapido protegido",
                    "El preview interactivo se ha degradado de forma intencional para mantener la navegacion fluida.",
                    "Protegido",
                    memoryLabel,
                    false
            );
        }

        return new DeepZoomAdvisory(
                "Preview profundo pesado",
                "Este preview preciso entra en una zona costosa de CPU y precision. Puede tardar bastante o parecer detenido. "
                        + (memoryPressureHigh
                        ? "Ademas la memoria JVM esta alta (" + memoryLabel + "). "
                        : "")
                        + "Si notas lentitud, baja iteraciones, usa el preview rapido o aleja el zoom antes de seguir profundizando.",
                memoryPressureHigh ? "Memoria alta" : "Zona pesada",
                memoryLabel,
                memoryPressureHigh || zoom >= 100_000_000d || costScore >= 350_000_000d
        );
    }
}
