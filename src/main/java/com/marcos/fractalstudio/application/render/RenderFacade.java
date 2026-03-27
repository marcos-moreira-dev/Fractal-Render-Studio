package com.marcos.fractalstudio.application.render;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;
import com.marcos.fractalstudio.application.preview.AdaptivePreviewQualityPolicy;
import com.marcos.fractalstudio.application.preview.GeneratePreviewUseCase;
import com.marcos.fractalstudio.application.preview.GenerateKeyframeThumbnailUseCase;
import com.marcos.fractalstudio.application.preview.PreviewTileUpdate;
import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.Resolution;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Stable application boundary for preview generation and background render jobs.
 */
public final class RenderFacade {

    private final GeneratePreviewUseCase generatePreviewUseCase;
    private final GenerateKeyframeThumbnailUseCase generateKeyframeThumbnailUseCase;
    private final SubmitRenderJobUseCase submitRenderJobUseCase;
    private final CancelRenderJobUseCase cancelRenderJobUseCase;
    private final ListRenderJobsUseCase listRenderJobsUseCase;

    public RenderFacade(
            GeneratePreviewUseCase generatePreviewUseCase,
            GenerateKeyframeThumbnailUseCase generateKeyframeThumbnailUseCase,
            SubmitRenderJobUseCase submitRenderJobUseCase,
            CancelRenderJobUseCase cancelRenderJobUseCase,
            ListRenderJobsUseCase listRenderJobsUseCase
    ) {
        this.generatePreviewUseCase = generatePreviewUseCase;
        this.generateKeyframeThumbnailUseCase = generateKeyframeThumbnailUseCase;
        this.submitRenderJobUseCase = submitRenderJobUseCase;
        this.cancelRenderJobUseCase = cancelRenderJobUseCase;
        this.listRenderJobsUseCase = listRenderJobsUseCase;
    }

    public CompletableFuture<RenderedFrame> generatePreview(
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan
    ) {
        return generatePreviewUseCase.generate(project, cameraState, previewRequestPlan);
    }

    public CompletableFuture<RenderedFrame> generatePreview(
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan,
            Consumer<PreviewTileUpdate> tileConsumer
    ) {
        return generatePreviewUseCase.generate(project, cameraState, previewRequestPlan, tileConsumer);
    }

    public void cancelActivePreview() {
        generatePreviewUseCase.cancelActivePreview();
    }

    public CompletableFuture<RenderedFrame> generateKeyframeThumbnail(Project project, CameraState cameraState) {
        return generateKeyframeThumbnailUseCase.generate(project, cameraState);
    }

    public CompletableFuture<RenderedFrame> generateCameraThumbnail(Project project, CameraState cameraState) {
        return generateKeyframeThumbnail(project, cameraState);
    }

    public void submitRender(RenderRequest renderRequest, Consumer<RenderJobStatusDto> statusConsumer) {
        submitRenderJobUseCase.submit(renderRequest, statusConsumer);
    }

    public boolean cancelRenderJob(String jobId, Consumer<RenderJobStatusDto> statusConsumer) {
        return cancelRenderJobUseCase.cancel(jobId, statusConsumer);
    }

    public java.util.List<RenderJobStatusDto> listRenderJobs() {
        return listRenderJobsUseCase.list();
    }
}
