package com.marcos.fractalstudio.presentation.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Owns the executor services used by preview, thumbnail generation and final rendering.
 *
 * <p>The desktop application intentionally keeps a small set of named pools instead of creating
 * ad-hoc threads inside individual services. Centralizing them here reduces wiring noise in the
 * composition root and makes shutdown policy explicit.
 */
final class StudioRuntimeExecutors {

    private final ExecutorService previewCoordinatorExecutorService;
    private final ExecutorService previewExecutorService;
    private final ExecutorService thumbnailExecutorService;
    private final ExecutorService renderExecutorService;

    StudioRuntimeExecutors(AppProperties appProperties) {
        this.previewCoordinatorExecutorService = Executors.newSingleThreadExecutor(
                namedThreadFactory("preview-coordinator")
        );
        this.previewExecutorService = Executors.newFixedThreadPool(
                appProperties.previewThreads(),
                namedThreadFactory("preview-worker")
        );
        this.thumbnailExecutorService = Executors.newSingleThreadExecutor(
                namedThreadFactory("timeline-thumbnail")
        );
        this.renderExecutorService = Executors.newFixedThreadPool(
                appProperties.renderThreads(),
                namedThreadFactory("render-worker")
        );
    }

    ExecutorService previewCoordinatorExecutorService() {
        return previewCoordinatorExecutorService;
    }

    ExecutorService previewExecutorService() {
        return previewExecutorService;
    }

    ExecutorService thumbnailExecutorService() {
        return thumbnailExecutorService;
    }

    ExecutorService renderExecutorService() {
        return renderExecutorService;
    }

    void shutdown() {
        shutdownExecutor(previewCoordinatorExecutorService);
        shutdownExecutor(previewExecutorService);
        shutdownExecutor(thumbnailExecutorService);
        shutdownExecutor(renderExecutorService);
    }

    private static ThreadFactory namedThreadFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("fractal-studio-" + prefix + "-" + counter.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
    }

    private static void shutdownExecutor(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                executorService.awaitTermination(2, TimeUnit.SECONDS);
            }
        } catch (InterruptedException exception) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
