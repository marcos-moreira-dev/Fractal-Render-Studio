package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;
import com.marcos.fractalstudio.domain.render.FrameIndex;
import com.marcos.fractalstudio.domain.render.RenderProfile;
import com.marcos.fractalstudio.domain.render.RenderQuality;
import com.marcos.fractalstudio.domain.render.Resolution;
import com.marcos.fractalstudio.domain.timeline.TimePosition;
import com.marcos.fractalstudio.infrastructure.rendering.TimelineThumbnailRenderer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Generates lightweight thumbnail previews for timeline keyframes on a dedicated background executor.
 */
public final class GenerateKeyframeThumbnailUseCase {

    private static final Resolution THUMBNAIL_RESOLUTION = new Resolution(160, 90);

    private final TimelineThumbnailRenderer thumbnailRenderer;
    private final Executor thumbnailExecutor;

    public GenerateKeyframeThumbnailUseCase(TimelineThumbnailRenderer thumbnailRenderer, Executor thumbnailExecutor) {
        this.thumbnailRenderer = thumbnailRenderer;
        this.thumbnailExecutor = thumbnailExecutor;
    }

    public CompletableFuture<RenderedFrame> generate(Project project, CameraState cameraState) {
        return CompletableFuture.supplyAsync(() -> thumbnailRenderer.render(new FrameDescriptor(
                new FrameIndex(0),
                new TimePosition(0.0),
                cameraState,
                project.fractalFormula(),
                new RenderProfile(
                        "Timeline Thumbnail",
                        THUMBNAIL_RESOLUTION,
                        new EscapeParameters(
                                Math.max(48, Math.min(96, project.renderProfile().escapeParameters().maxIterations())),
                                project.renderProfile().escapeParameters().escapeRadius()
                        ),
                        RenderQuality.PREVIEW
                ),
                project.colorProfile()
        )), thumbnailExecutor);
    }
}
