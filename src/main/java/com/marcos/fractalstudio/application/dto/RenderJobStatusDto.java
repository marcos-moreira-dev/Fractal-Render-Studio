package com.marcos.fractalstudio.application.dto;

public record RenderJobStatusDto(
        String jobId,
        String jobName,
        RenderJobState state,
        int completedFrames,
        int totalFrames,
        double progress,
        String message,
        String outputDirectory
) {
}
