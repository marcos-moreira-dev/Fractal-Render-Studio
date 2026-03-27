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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Generates a preview frame by partitioning the viewport into tiles and rendering them in parallel.
 */
public final class GeneratePreviewUseCase {

    private static final int TILE_SIZE = 128;

    private final TiledPreviewRenderer previewRenderer;
    private final Executor previewCoordinatorExecutor;
    private final ExecutorService previewExecutorService;
    private final Object activePreviewLock = new Object();
    private PreviewOperation activePreviewOperation;

    public GeneratePreviewUseCase(
            TiledPreviewRenderer previewRenderer,
            Executor previewCoordinatorExecutor,
            ExecutorService previewExecutorService
    ) {
        this.previewRenderer = previewRenderer;
        this.previewCoordinatorExecutor = previewCoordinatorExecutor;
        this.previewExecutorService = previewExecutorService;
    }

    public CompletableFuture<RenderedFrame> generate(
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan
    ) {
        return generate(project, cameraState, previewRequestPlan, ignored -> {
        });
    }

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

    public void cancelActivePreview() {
        PreviewOperation operation;
        synchronized (activePreviewLock) {
            operation = activePreviewOperation;
        }
        if (operation != null) {
            operation.cancel();
        }
    }

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
        } catch (Exception exception) {
            throw new IllegalStateException("Preview rendering failed.", exception);
        }

        return new RenderedFrame(resolution.width(), resolution.height(), framePixels);
    }

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
