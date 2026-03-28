package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;
import com.marcos.fractalstudio.domain.render.FrameIndex;
import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.RenderProfile;
import com.marcos.fractalstudio.domain.render.RenderQuality;
import com.marcos.fractalstudio.domain.render.Resolution;
import com.marcos.fractalstudio.domain.timeline.TimePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Generates interactive preview frames by partitioning the visible viewport
 * into tiles and rendering those tiles in parallel.
 *
 * <p>This use case is one of the places where the product-level idea of
 * "agents rendering different sectors of the fractal" becomes concrete. Each
 * preview request is decomposed into tile tasks that can be processed by the
 * preview executor pool, merged into a single frame and cancelled when the user
 * changes camera state before the previous request finishes.
 */
public final class GeneratePreviewUseCase {

    private static final int TILE_SIZE = 128;

    private final TiledPreviewRenderer previewRenderer;
    private final Executor previewCoordinatorExecutor;
    private final ExecutorService previewExecutorService;
    private final Object activePreviewLock = new Object();
    private PreviewOperation activePreviewOperation;

    /**
     * Creates the preview generator used by the shell coordinator.
     *
     * @param previewRenderer renderer capable of producing tile-level results
     * @param previewCoordinatorExecutor executor that serializes high-level preview requests
     * @param previewExecutorService worker pool that computes tile content
     */
    public GeneratePreviewUseCase(
            TiledPreviewRenderer previewRenderer,
            Executor previewCoordinatorExecutor,
            ExecutorService previewExecutorService
    ) {
        this.previewRenderer = previewRenderer;
        this.previewCoordinatorExecutor = previewCoordinatorExecutor;
        this.previewExecutorService = previewExecutorService;
    }

    /**
     * Starts a preview request without exposing incremental tile updates to the
     * caller.
     *
     * @param project current project aggregate
     * @param cameraState camera state that defines the visible fractal region
     * @param previewRequestPlan adaptive execution plan chosen for this preview
     * @return future completed with the merged preview frame
     */
    public CompletableFuture<RenderedFrame> generate(
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan
    ) {
        return generate(project, cameraState, previewRequestPlan, ignored -> {
        });
    }

    /**
     * Starts a preview request and reports each rendered tile as it becomes
     * available.
     *
     * <p>The tile consumer is intended for progressive UI updates. The final
     * returned future still completes with the fully merged frame once every
     * visible tile has finished.
     */
    public CompletableFuture<RenderedFrame> generate(
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan,
            Consumer<PreviewTileUpdate> tileConsumer
    ) {
        FrameDescriptor frameDescriptor = buildFrameDescriptor(project, cameraState, previewRequestPlan);
        PreviewOperation operation = new PreviewOperation(new PreviewCancellationToken());
        synchronized (activePreviewLock) {
            activePreviewOperation = operation;
        }

        CompletableFuture<RenderedFrame> future = CompletableFuture.supplyAsync(
                () -> renderProgressively(frameDescriptor, tileConsumer, operation),
                previewCoordinatorExecutor
        );
        operation.rootFuture = future;
        future.whenComplete((ignoredResult, ignoredThrowable) -> clearActivePreviewOperation(operation));
        return future;
    }

    /**
     * Cancels the currently active preview operation, if any.
     *
     * <p>Cancellation is cooperative. Already running tile tasks receive a
     * token and may also be interrupted via their futures so that obsolete
     * previews stop competing against newer camera requests.
     */
    public void cancelActivePreview() {
        PreviewOperation operation;
        synchronized (activePreviewLock) {
            operation = activePreviewOperation;
        }
        if (operation != null) {
            operation.cancel();
        }
    }

    /**
     * Renders the current preview progressively by submitting one task per tile
     * and merging completed results in completion order.
     *
     * <p>Completion order matters for perceived responsiveness because the UI
     * can update tile-by-tile instead of waiting for a monolithic frame
     * computation.
     */
    private RenderedFrame renderProgressively(
            FrameDescriptor frameDescriptor,
            Consumer<PreviewTileUpdate> tileConsumer,
            PreviewOperation operation
    ) {
        Resolution resolution = frameDescriptor.renderProfile().resolution();
        int[] framePixels = new int[resolution.width() * resolution.height()];
        List<TileBounds> tiles = partitionTiles(resolution);
        CompletionService<PreviewTileUpdate> completionService = new ExecutorCompletionService<>(previewExecutorService);

        for (TileBounds tileBounds : tiles) {
            operation.tileFutures.add(completionService.submit(() -> previewRenderer.renderTile(
                    frameDescriptor,
                    tileBounds.tileX(),
                    tileBounds.tileY(),
                    tileBounds.tileWidth(),
                    tileBounds.tileHeight(),
                    operation.cancellationToken
            )));
        }

        int completedTiles = 0;
        try {
            while (completedTiles < tiles.size()) {
                operation.cancellationToken.throwIfCancelled();
                PreviewTileUpdate renderedTile = completionService.take().get();
                operation.cancellationToken.throwIfCancelled();
                completedTiles++;
                PreviewTileUpdate tileUpdate = new PreviewTileUpdate(
                        renderedTile.frameWidth(),
                        renderedTile.frameHeight(),
                        renderedTile.tileX(),
                        renderedTile.tileY(),
                        renderedTile.tileWidth(),
                        renderedTile.tileHeight(),
                        renderedTile.argbPixels(),
                        completedTiles,
                        tiles.size()
                );
                mergeTile(framePixels, resolution.width(), tileUpdate);
                tileConsumer.accept(tileUpdate);
            }
        } catch (CancellationException cancellationException) {
            throw cancellationException;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Preview rendering was interrupted.");
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Preview rendering failed.", exception);
        }

        return new RenderedFrame(resolution.width(), resolution.height(), framePixels);
    }

    /**
     * Builds the preview-specific frame descriptor derived from the current
     * project and camera state.
     *
     * <p>The descriptor intentionally overrides only the parts that should vary
     * for preview execution, namely effective resolution, iteration budget and
     * render quality. Formula and color profile remain those of the project.
     */
    private FrameDescriptor buildFrameDescriptor(
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan
    ) {
        RenderProfile previewProfile = new RenderProfile(
                previewRequestPlan.profileName(),
                previewRequestPlan.resolution(),
                new EscapeParameters(
                        previewRequestPlan.maxIterations(),
                        project.renderProfile().escapeParameters().escapeRadius()
                ),
                RenderQuality.PREVIEW
        );

        return new FrameDescriptor(
                new FrameIndex(0),
                new TimePosition(0.0),
                cameraState,
                project.fractalFormula(),
                previewProfile,
                project.colorProfile()
        );
    }

    /**
     * Splits the requested preview resolution into fixed-size rectangular tiles.
     *
     * <p>Uniform tiles simplify load distribution and progress accounting while
     * still allowing the last tile of each row or column to shrink at the
     * borders.
     */
    private List<TileBounds> partitionTiles(Resolution resolution) {
        List<TileBounds> tiles = new ArrayList<>();
        for (int tileY = 0; tileY < resolution.height(); tileY += TILE_SIZE) {
            int tileHeight = Math.min(TILE_SIZE, resolution.height() - tileY);
            for (int tileX = 0; tileX < resolution.width(); tileX += TILE_SIZE) {
                int tileWidth = Math.min(TILE_SIZE, resolution.width() - tileX);
                tiles.add(new TileBounds(tileX, tileY, tileWidth, tileHeight));
            }
        }
        return tiles;
    }

    /**
     * Copies a rendered tile into the final frame buffer at the tile's target
     * position.
     */
    private void mergeTile(int[] framePixels, int frameWidth, PreviewTileUpdate tileUpdate) {
        for (int localY = 0; localY < tileUpdate.tileHeight(); localY++) {
            int sourceOffset = localY * tileUpdate.tileWidth();
            int targetOffset = ((tileUpdate.tileY() + localY) * frameWidth) + tileUpdate.tileX();
            System.arraycopy(tileUpdate.argbPixels(), sourceOffset, framePixels, targetOffset, tileUpdate.tileWidth());
        }
    }

    private void clearActivePreviewOperation(PreviewOperation operation) {
        synchronized (activePreviewLock) {
            if (activePreviewOperation == operation) {
                activePreviewOperation = null;
            }
        }
    }

    private record TileBounds(int tileX, int tileY, int tileWidth, int tileHeight) {
    }

    /**
     * Mutable holder for the currently active preview operation and all task
     * handles that belong to it.
     *
     * <p>This type is intentionally private because it is an execution detail of
     * preview coordination, not part of the application boundary.
     */
    private static final class PreviewOperation {

        private final PreviewCancellationToken cancellationToken;
        private final List<Future<?>> tileFutures = new ArrayList<>();
        private CompletableFuture<RenderedFrame> rootFuture;

        private PreviewOperation(PreviewCancellationToken cancellationToken) {
            this.cancellationToken = cancellationToken;
        }

        private void cancel() {
            cancellationToken.cancel();
            for (Future<?> tileFuture : tileFutures) {
                tileFuture.cancel(true);
            }
            if (rootFuture != null) {
                rootFuture.cancel(true);
            }
        }
    }
}
