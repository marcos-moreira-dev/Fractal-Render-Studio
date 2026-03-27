package com.marcos.fractalstudio.infrastructure.metrics;

import java.time.Duration;
import java.time.Instant;

public final class BatchMetricsCollector {

    public Duration measure(Instant startedAt, Instant finishedAt) {
        if (startedAt == null || finishedAt == null) {
            return Duration.ZERO;
        }
        return Duration.between(startedAt, finishedAt);
    }
}
