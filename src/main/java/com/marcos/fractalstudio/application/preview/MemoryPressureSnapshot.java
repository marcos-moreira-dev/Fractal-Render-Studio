package com.marcos.fractalstudio.application.preview;

/**
 * Lightweight JVM heap snapshot used to enrich deep-zoom advisories with real
 * memory pressure information.
 */
public record MemoryPressureSnapshot(
        long usedBytes,
        long committedBytes,
        long maxBytes
) {

    /**
     * @return used heap divided by the maximum heap capacity
     */
    public double usageRatio() {
        if (maxBytes <= 0L) {
            return 0.0;
        }
        return (double) usedBytes / (double) maxBytes;
    }

    /**
     * @return remaining heap capacity before reaching the JVM max heap
     */
    public long availableBytes() {
        return Math.max(0L, maxBytes - usedBytes);
    }

    /**
     * @return compact memory label suitable for the inspector or warning dialogs
     */
    public String compactLabel() {
        return formatMegabytes(usedBytes) + " MB / " + formatMegabytes(maxBytes) + " MB";
    }

    private static long formatMegabytes(long bytes) {
        return Math.max(0L, Math.round(bytes / 1_048_576d));
    }
}
