package com.marcos.fractalstudio.infrastructure.batching;

import java.util.concurrent.atomic.AtomicBoolean;

public final class JobCancellationToken {

    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);

    public void cancel() {
        cancellationRequested.set(true);
    }

    public boolean isCancellationRequested() {
        return cancellationRequested.get();
    }
}
