package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.application.project.CreateProjectUseCase;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;
import com.marcos.fractalstudio.domain.render.Resolution;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GeneratePreviewUseCaseTest {

    @Test
    void rendersPreviewProgressivelyByTiles() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            GeneratePreviewUseCase useCase = new GeneratePreviewUseCase(new StubPreviewRenderer(), Runnable::run, executorService);
            Project project = new CreateProjectUseCase().create("Preview Test");
            CameraState cameraState = new CameraState(new FractalCoordinate(-0.5, 0.0), new ZoomLevel(1.0));
            List<PreviewTileUpdate> updates = new CopyOnWriteArrayList<>();

            RenderedFrame renderedFrame = useCase.generate(
                    project,
                    cameraState,
                    AdaptivePreviewQualityPolicy.plan(260, 130, cameraState.zoomLevel().value(), project.renderProfile().escapeParameters().maxIterations(), true),
                    updates::add
            ).join();

            assertEquals(260, renderedFrame.width());
            assertEquals(130, renderedFrame.height());
            assertEquals(6, updates.size());
            assertEquals(1, updates.getFirst().completedTiles());
            assertEquals(6, updates.getLast().completedTiles());
            assertTrue(updates.stream().allMatch(update -> update.totalTiles() == 6));
            assertEquals(colorForTile(0, 0), renderedFrame.argbPixels()[0]);
            assertEquals(colorForTile(128, 0), renderedFrame.argbPixels()[128]);
            assertEquals(colorForTile(256, 128), renderedFrame.argbPixels()[(129 * renderedFrame.width()) + 259]);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void cancelsObsoletePreviewBeforeReplacementStarts() {
        ExecutorService coordinatorExecutor = Executors.newSingleThreadExecutor();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            AtomicBoolean blockTiles = new AtomicBoolean(true);
            GeneratePreviewUseCase useCase = new GeneratePreviewUseCase(
                    new BlockingPreviewRenderer(blockTiles),
                    coordinatorExecutor,
                    executorService
            );
            Project project = new CreateProjectUseCase().create("Preview Cancellation Test");
            CameraState cameraState = new CameraState(new FractalCoordinate(-0.5, 0.0), new ZoomLevel(1.0));

            CompletableFuture<RenderedFrame> firstPreview = useCase.generate(
                    project,
                    cameraState,
                    AdaptivePreviewQualityPolicy.plan(256, 256, cameraState.zoomLevel().value(), project.renderProfile().escapeParameters().maxIterations(), true)
            );


            useCase.cancelActivePreview();
            blockTiles.set(false);

            assertThrows(CancellationException.class, firstPreview::join);

            RenderedFrame secondPreview = useCase.generate(
                    project,
                    new CameraState(new FractalCoordinate(-0.25, 0.0), new ZoomLevel(1.4)),
                    AdaptivePreviewQualityPolicy.plan(256, 256, 1.4, project.renderProfile().escapeParameters().maxIterations(), true)
            ).join();

            assertEquals(256, secondPreview.width());
            assertEquals(256, secondPreview.height());
        } finally {
            coordinatorExecutor.shutdownNow();
            executorService.shutdownNow();
        }
    }

    private static int colorForTile(int tileX, int tileY) {
        int red = (tileX / 16) & 0xFF;
        int green = (tileY / 16) & 0xFF;
        int blue = 0x7F;
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static class StubPreviewRenderer implements TiledPreviewRenderer {

        @Override
        public RenderedFrame render(FrameDescriptor frameDescriptor) {
            Resolution resolution = frameDescriptor.renderProfile().resolution();
            return new RenderedFrame(resolution.width(), resolution.height(), new int[resolution.width() * resolution.height()]);
        }

        @Override
        public PreviewTileUpdate renderTile(FrameDescriptor frameDescriptor, int tileX, int tileY, int tileWidth, int tileHeight) {
            int[] pixels = new int[tileWidth * tileHeight];
            Arrays.fill(pixels, colorForTile(tileX, tileY));
            Resolution resolution = frameDescriptor.renderProfile().resolution();
            return new PreviewTileUpdate(
                    resolution.width(),
                    resolution.height(),
                    tileX,
                    tileY,
                    tileWidth,
                    tileHeight,
                    pixels,
                    1,
                    1
            );
        }
    }

    private static final class BlockingPreviewRenderer extends StubPreviewRenderer {

        private final AtomicBoolean blockTiles;

        private BlockingPreviewRenderer(AtomicBoolean blockTiles) {
            this.blockTiles = blockTiles;
        }

        @Override
        public PreviewTileUpdate renderTile(
                FrameDescriptor frameDescriptor,
                int tileX,
                int tileY,
                int tileWidth,
                int tileHeight,
                PreviewCancellationToken cancellationToken
        ) {
            while (blockTiles.get()) {
                cancellationToken.throwIfCancelled();
                Thread.onSpinWait();
            }
            cancellationToken.throwIfCancelled();
            return super.renderTile(frameDescriptor, tileX, tileY, tileWidth, tileHeight, cancellationToken);
        }
    }
}
