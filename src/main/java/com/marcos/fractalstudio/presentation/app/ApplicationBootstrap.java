package com.marcos.fractalstudio.presentation.app;

import com.marcos.fractalstudio.application.render.CancelRenderJobUseCase;
import com.marcos.fractalstudio.application.export.ExportFacade;
import com.marcos.fractalstudio.application.export.ExportFramesUseCase;
import com.marcos.fractalstudio.application.renderhistory.LoadRenderHistoryUseCase;
import com.marcos.fractalstudio.application.renderhistory.RenderHistoryFacade;
import com.marcos.fractalstudio.application.render.ListRenderJobsUseCase;
import com.marcos.fractalstudio.application.project.LoadProjectUseCase;
import com.marcos.fractalstudio.application.project.ProjectFacade;
import com.marcos.fractalstudio.application.project.RenameProjectUseCase;
import com.marcos.fractalstudio.application.project.AddBookmarkUseCase;
import com.marcos.fractalstudio.application.project.CreateKeyframeFromBookmarkUseCase;
import com.marcos.fractalstudio.application.project.DeleteBookmarkUseCase;
import com.marcos.fractalstudio.application.project.MoveBookmarkUseCase;
import com.marcos.fractalstudio.application.project.RenameBookmarkUseCase;
import com.marcos.fractalstudio.application.project.UpdateProjectInspectorUseCase;
import com.marcos.fractalstudio.application.renderhistory.SaveRenderHistoryUseCase;
import com.marcos.fractalstudio.application.project.SaveProjectUseCase;
import com.marcos.fractalstudio.application.project.UpdateProjectMetadataUseCase;
import com.marcos.fractalstudio.application.project.UpdateProjectSettingsUseCase;
import com.marcos.fractalstudio.application.render.RenderFacade;
import com.marcos.fractalstudio.application.preview.GenerateKeyframeThumbnailUseCase;
import com.marcos.fractalstudio.application.preview.GeneratePreviewUseCase;
import com.marcos.fractalstudio.application.project.CreateProjectUseCase;
import com.marcos.fractalstudio.application.timeline.AddKeyframeUseCase;
import com.marcos.fractalstudio.application.timeline.BuildTimelineSequenceUseCase;
import com.marcos.fractalstudio.application.timeline.DeleteKeyframeUseCase;
import com.marcos.fractalstudio.application.timeline.RenameKeyframeUseCase;
import com.marcos.fractalstudio.application.render.SubmitRenderJobUseCase;
import com.marcos.fractalstudio.infrastructure.batching.InMemoryRenderQueue;
import com.marcos.fractalstudio.infrastructure.batching.WorkerPoolManager;
import com.marcos.fractalstudio.infrastructure.export.PngFrameSequenceExporter;
import com.marcos.fractalstudio.infrastructure.export.Mp4SequenceVideoExporter;
import com.marcos.fractalstudio.infrastructure.export.ZipSequenceArchiveExporter;
import com.marcos.fractalstudio.infrastructure.metrics.BatchMetricsCollector;
import com.marcos.fractalstudio.infrastructure.persistence.ProjectFileRepository;
import com.marcos.fractalstudio.infrastructure.persistence.RenderHistoryFileRepository;
import com.marcos.fractalstudio.infrastructure.rendering.FinalFrameRenderer;
import com.marcos.fractalstudio.infrastructure.rendering.FrameRendererFactory;
import com.marcos.fractalstudio.infrastructure.rendering.PreviewFrameRenderer;
import com.marcos.fractalstudio.infrastructure.rendering.TimelineThumbnailRenderer;
import com.marcos.fractalstudio.presentation.common.UiThreadExecutor;
import com.marcos.fractalstudio.presentation.shell.StudioShellViewModel;

import javafx.scene.Parent;

import java.nio.file.Path;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Assembles application services, infrastructure adapters and the UI composition root.
 */
public final class ApplicationBootstrap {

    private final AppProperties appProperties = new AppPropertiesLoader().load();
    private final ExecutorService previewCoordinatorExecutorService = Executors.newSingleThreadExecutor(
            namedThreadFactory("preview-coordinator")
    );
    private final ExecutorService previewExecutorService = Executors.newFixedThreadPool(
            appProperties.previewThreads(),
            namedThreadFactory("preview-worker")
    );
    private final ExecutorService thumbnailExecutorService = Executors.newSingleThreadExecutor(
            namedThreadFactory("timeline-thumbnail")
    );
    private final ExecutorService renderExecutorService = Executors.newFixedThreadPool(
            appProperties.renderThreads(),
            namedThreadFactory("render-worker")
    );
    private StudioShellViewModel studioShellViewModel;

    /**
     * Builds the root JavaFX node and the service graph backing it.
     *
     * @return application root node
     */
    public Parent build() {
        PreviewFrameRenderer previewFrameRenderer = new PreviewFrameRenderer();
        TimelineThumbnailRenderer timelineThumbnailRenderer = new TimelineThumbnailRenderer();
        FinalFrameRenderer finalFrameRenderer = new FinalFrameRenderer();
        FrameRendererFactory frameRendererFactory = new FrameRendererFactory(previewFrameRenderer, finalFrameRenderer);
        UiThreadExecutor uiThreadExecutor = new UiThreadExecutor();
        ProjectFileRepository projectFileRepository = new ProjectFileRepository();
        RenderHistoryFileRepository renderHistoryFileRepository = new RenderHistoryFileRepository();
        WorkerPoolManager renderJobGateway = new WorkerPoolManager(
                frameRendererFactory,
                new PngFrameSequenceExporter(),
                new Mp4SequenceVideoExporter(),
                renderExecutorService,
                new InMemoryRenderQueue(),
                new BatchMetricsCollector()
        );

        ProjectFacade projectFacade = new ProjectFacade(
                new CreateProjectUseCase(),
                new AddKeyframeUseCase(),
                new RenameKeyframeUseCase(),
                new DeleteKeyframeUseCase(),
                new AddBookmarkUseCase(),
                new DeleteBookmarkUseCase(),
                new RenameBookmarkUseCase(),
                new MoveBookmarkUseCase(),
                new CreateKeyframeFromBookmarkUseCase(new AddKeyframeUseCase()),
                new UpdateProjectInspectorUseCase(),
                new RenameProjectUseCase(),
                new UpdateProjectMetadataUseCase(),
                new UpdateProjectSettingsUseCase(),
                new SaveProjectUseCase(projectFileRepository),
                new LoadProjectUseCase(projectFileRepository)
        );

        RenderFacade renderFacade = new RenderFacade(
                new GeneratePreviewUseCase(previewFrameRenderer, previewCoordinatorExecutorService, previewExecutorService),
                new GenerateKeyframeThumbnailUseCase(timelineThumbnailRenderer, thumbnailExecutorService),
                new SubmitRenderJobUseCase(
                        new BuildTimelineSequenceUseCase(),
                        renderJobGateway
                ),
                new CancelRenderJobUseCase(renderJobGateway),
                new ListRenderJobsUseCase(renderJobGateway)
        );

        ExportFacade exportFacade = new ExportFacade(
                new ExportFramesUseCase(new ZipSequenceArchiveExporter())
        );

        RenderHistoryFacade renderHistoryFacade = new RenderHistoryFacade(
                new LoadRenderHistoryUseCase(renderHistoryFileRepository),
                new SaveRenderHistoryUseCase(renderHistoryFileRepository)
        );

        studioShellViewModel = new StudioShellViewModel(
                projectFacade,
                renderFacade,
                renderHistoryFacade,
                exportFacade,
                uiThreadExecutor,
                Path.of(appProperties.storageRoot())
        );

        return new UiCompositionRoot().compose(studioShellViewModel);
    }

    /**
     * Returns the effective runtime settings.
     *
     * @return runtime properties loaded from configuration
     */
    public AppProperties appProperties() {
        return appProperties;
    }

    /**
     * Stops background executors used by preview and final rendering.
     */
    public void shutdown() {
        if (studioShellViewModel != null) {
            studioShellViewModel.shutdownSession();
        }
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
