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
 * Stable application boundary for preview generation, thumbnails and background render jobs.
 *
 * <p>The presentation layer should not need to know whether a frame comes from a fast preview policy,
 * a tile-based progressive render, a thumbnail renderer or a long-running video job. This facade keeps
 * those workflows behind a single boundary so JavaFX views can talk in product concepts such as
 * "preview the current viewport", "generate the thumbnail for this point" or "submit a video render"
 * instead of wiring individual use cases together.
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

    /**
     * Generates a full preview frame for the current viewport.
     *
     * <p>This overload is used when the caller only needs the final frame once all tiles have been
     * assembled.
     */
    public CompletableFuture<RenderedFrame> generatePreview(
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan
    ) {
        return generatePreviewUseCase.generate(project, cameraState, previewRequestPlan);
    }

    /**
     * Generates a preview frame while streaming tile updates to the caller.
     *
     * <p>This variant exists for the progressive explorer experience where the viewport can be updated
     * sector by sector before the whole frame is complete.
     */
    public CompletableFuture<RenderedFrame> generatePreview(
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan,
            Consumer<PreviewTileUpdate> tileConsumer
    ) {
        return generatePreviewUseCase.generate(project, cameraState, previewRequestPlan, tileConsumer);
    }

    /**
     * Cancels the preview currently owned by the render subsystem.
     *
     * <p>Cancellation is cooperative and is mainly used when the user changes camera state faster than
     * the renderer can complete the previous request.
     */
    public void cancelActivePreview() {
        generatePreviewUseCase.cancelActivePreview();
    }

    /**
     * Generates the small preview used by timeline cards and point lists.
     */
    public CompletableFuture<RenderedFrame> generateKeyframeThumbnail(Project project, CameraState cameraState) {
        return generateKeyframeThumbnailUseCase.generate(project, cameraState);
    }

    /**
     * Semantic alias for thumbnail generation when the camera source is not literally a keyframe.
     */
    public CompletableFuture<RenderedFrame> generateCameraThumbnail(Project project, CameraState cameraState) {
        return generateKeyframeThumbnail(project, cameraState);
    }

    /**
     * Starts a background render job, typically for MP4 export.
     */
    public void submitRender(RenderRequest renderRequest, Consumer<RenderJobStatusDto> statusConsumer) {
        submitRenderJobUseCase.submit(renderRequest, statusConsumer);
    }

    /**
     * Requests cancellation of a queued or running render job.
     */
    public boolean cancelRenderJob(String jobId, Consumer<RenderJobStatusDto> statusConsumer) {
        return cancelRenderJobUseCase.cancel(jobId, statusConsumer);
    }

    /**
     * Lists the observable status of all known render jobs.
     */
    public java.util.List<RenderJobStatusDto> listRenderJobs() {
        return listRenderJobsUseCase.list();
    }
}
