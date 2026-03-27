package com.marcos.fractalstudio.application.preview;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cooperative cancellation token for preview rendering tasks.
 */
public final class PreviewCancellationToken {

    private final AtomicBoolean cancelled = new AtomicBoolean();

    public void cancel() {
        cancelled.set(true);
    }

    public void throwIfCancelled() {
        if (cancelled.get() || Thread.currentThread().isInterrupted()) {
            throw new CancellationException("Preview rendering was cancelled.");
        }
    }
}
