package com.marcos.fractalstudio.infrastructure.batching;

public enum JobState {
    QUEUED,
    PREPARING,
    RENDERING,
    CANCELLED,
    COMPLETED,
    FAILED
}
