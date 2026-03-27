package com.marcos.fractalstudio.infrastructure.persistence;

public record RenderJobHistoryDocument(
        String jobId,
        String jobName,
        String state,
        int completedFrames,
        int totalFrames,
        double progress,
        String message,
        String outputDirectory
) {
}
