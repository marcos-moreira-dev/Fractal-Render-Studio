package com.marcos.fractalstudio.infrastructure.batching;

import java.nio.file.Path;
import java.time.Instant;

public final class RenderJob {

    private final RenderJobId id;
    private final String name;
    private final Path outputDirectory;
    private final int totalFrames;
    private final JobCancellationToken cancellationToken;
    private final Instant createdAt;
    private volatile JobState state;
    private volatile int completedFrames;
    private volatile String message;
    private volatile boolean cancellationRequested;
    private volatile Instant startedAt;
    private volatile Instant finishedAt;

    public RenderJob(RenderJobId id, String name, Path outputDirectory, int totalFrames, JobCancellationToken cancellationToken) {
        this.id = id;
        this.name = name;
        this.outputDirectory = outputDirectory;
        this.totalFrames = totalFrames;
        this.cancellationToken = cancellationToken;
        this.createdAt = Instant.now();
        this.state = JobState.QUEUED;
        this.message = "Job encolado";
    }

    public RenderJobId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Path outputDirectory() {
        return outputDirectory;
    }

    public int totalFrames() {
        return totalFrames;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public JobState state() {
        return state;
    }

    public int completedFrames() {
        return completedFrames;
    }

    public String message() {
        return message;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant finishedAt() {
        return finishedAt;
    }

    public JobCancellationToken cancellationToken() {
        return cancellationToken;
    }

    public boolean cancellationRequested() {
        return cancellationRequested;
    }

    public double progress() {
        return totalFrames == 0 ? 0.0 : completedFrames / (double) totalFrames;
    }

    public void markPreparing(String jobMessage) {
        state = JobState.PREPARING;
        startedAt = Instant.now();
        message = jobMessage;
    }

    public void markRendering(int framesCompleted, String jobMessage) {
        state = JobState.RENDERING;
        completedFrames = framesCompleted;
        message = jobMessage;
    }

    public void markCompleted(String jobMessage) {
        state = JobState.COMPLETED;
        completedFrames = totalFrames;
        message = jobMessage;
        finishedAt = Instant.now();
    }

    public void markFailed(String jobMessage) {
        state = JobState.FAILED;
        message = jobMessage;
        finishedAt = Instant.now();
    }

    public void markCancellationRequested(String jobMessage) {
        cancellationRequested = true;
        message = jobMessage;
    }

    public void markCancelled(String jobMessage) {
        state = JobState.CANCELLED;
        cancellationRequested = true;
        message = jobMessage;
        finishedAt = Instant.now();
    }
}
