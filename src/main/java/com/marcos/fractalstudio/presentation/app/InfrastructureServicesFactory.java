package com.marcos.fractalstudio.presentation.app;

import com.marcos.fractalstudio.infrastructure.batching.InMemoryRenderQueue;
import com.marcos.fractalstudio.infrastructure.batching.WorkerPoolManager;
import com.marcos.fractalstudio.infrastructure.export.Mp4SequenceVideoExporter;
import com.marcos.fractalstudio.infrastructure.export.PngFrameSequenceExporter;
import com.marcos.fractalstudio.infrastructure.metrics.BatchMetricsCollector;
import com.marcos.fractalstudio.infrastructure.persistence.ProjectFileRepository;
import com.marcos.fractalstudio.infrastructure.rendering.FinalFrameRenderer;
import com.marcos.fractalstudio.infrastructure.rendering.FrameRendererFactory;
import com.marcos.fractalstudio.infrastructure.rendering.PreviewFrameRenderer;

/**
 * Creates the infrastructure adapters required by the desktop application.
 *
 * <p>This factory centralizes construction of concrete repositories, exporters and render gateways so
 * the bootstrap class can remain focused on high-level composition instead of low-level dependency
 * assembly.
 */
final class InfrastructureServicesFactory {

    InfrastructureServices create(StudioRuntimeExecutors executors) {
        PreviewFrameRenderer previewFrameRenderer = new PreviewFrameRenderer();
        FinalFrameRenderer finalFrameRenderer = new FinalFrameRenderer();
        FrameRendererFactory frameRendererFactory = new FrameRendererFactory(previewFrameRenderer, finalFrameRenderer);

        WorkerPoolManager renderJobGateway = new WorkerPoolManager(
                frameRendererFactory,
                new PngFrameSequenceExporter(),
                new Mp4SequenceVideoExporter(),
                executors.renderExecutorService(),
                new InMemoryRenderQueue(),
                new BatchMetricsCollector()
        );

        return new InfrastructureServices(
                new ProjectFileRepository(),
                renderJobGateway
        );
    }
}
