package com.marcos.fractalstudio.infrastructure.batching;

import com.marcos.fractalstudio.application.dto.RenderJobState;
import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;
import com.marcos.fractalstudio.application.preview.PreviewRenderer;
import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.application.render.RenderPlan;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;
import com.marcos.fractalstudio.domain.color.ColorProfile;
import com.marcos.fractalstudio.domain.color.ColorStop;
import com.marcos.fractalstudio.domain.color.Palette;
import com.marcos.fractalstudio.domain.color.RgbColor;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaFactory;
import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;
import com.marcos.fractalstudio.domain.render.FrameIndex;
import com.marcos.fractalstudio.domain.render.RenderProfile;
import com.marcos.fractalstudio.domain.render.RenderQuality;
import com.marcos.fractalstudio.domain.render.Resolution;
import com.marcos.fractalstudio.domain.timeline.TimePosition;
import com.marcos.fractalstudio.infrastructure.export.FrameSequenceExporter;
import com.marcos.fractalstudio.infrastructure.export.SequenceVideoExporter;
import com.marcos.fractalstudio.infrastructure.metrics.BatchMetricsCollector;
import com.marcos.fractalstudio.infrastructure.rendering.FrameRendererFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorkerPoolManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void cancelsJobCooperatively() throws Exception {
        PreviewRenderer slowRenderer = frameDescriptor -> {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(exception);
            }
            return new RenderedFrame(10, 10, new int[100]);
        };
        FrameSequenceExporter noOpExporter = (outputDirectory, frameDescriptor, renderedFrame) -> {
        };
        SequenceVideoExporter noOpVideoExporter = (sourceDirectory, destinationVideo, framesPerSecond) -> destinationVideo;

        WorkerPoolManager workerPoolManager = new WorkerPoolManager(
                new FrameRendererFactory(slowRenderer::render, slowRenderer::render),
                noOpExporter,
                noOpVideoExporter,
                Executors.newSingleThreadExecutor(),
                new InMemoryRenderQueue(),
                new BatchMetricsCollector()
        );

        List<RenderJobStatusDto> statuses = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        workerPoolManager.submit(
                new RenderPlan("cancel-test", tempDir, 24.0, List.of(sampleFrameDescriptor())),
                status -> {
                    statuses.add(status);
                    if (status.state() == RenderJobState.CANCELLED) {
                        latch.countDown();
                    }
                }
        );

        String jobId = statuses.getFirst().jobId();
        workerPoolManager.cancel(jobId, status -> {
            statuses.add(status);
            if (status.state() == RenderJobState.CANCELLED) {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(statuses.stream().anyMatch(status -> status.state() == RenderJobState.CANCELLED));
    }

    @Test
    void storesTemporaryFramesInsideDedicatedFramesDirectory() throws Exception {
        PreviewRenderer fastRenderer = frameDescriptor -> new RenderedFrame(10, 10, new int[100]);
        List<Path> exportedDirectories = new CopyOnWriteArrayList<>();
        FrameSequenceExporter exporter = (outputDirectory, frameDescriptor, renderedFrame) -> exportedDirectories.add(outputDirectory);
        SequenceVideoExporter noOpVideoExporter = (sourceDirectory, destinationVideo, framesPerSecond) -> destinationVideo;

        WorkerPoolManager workerPoolManager = new WorkerPoolManager(
                new FrameRendererFactory(fastRenderer::render, fastRenderer::render),
                exporter,
                noOpVideoExporter,
                Executors.newSingleThreadExecutor(),
                new InMemoryRenderQueue(),
                new BatchMetricsCollector()
        );

        CountDownLatch latch = new CountDownLatch(1);
        workerPoolManager.submit(
                new RenderPlan("frames-dir-test", tempDir, 24.0, List.of(sampleFrameDescriptor())),
                status -> {
                    if (status.state() == RenderJobState.COMPLETED) {
                        latch.countDown();
                    }
                }
        );

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(exportedDirectories.stream().allMatch(path -> path.endsWith("frames")));
    }

    private FrameDescriptor sampleFrameDescriptor() {
        return new FrameDescriptor(
                new FrameIndex(0),
                new TimePosition(0.0),
                new CameraState(new FractalCoordinate(-0.5, 0.0), new ZoomLevel(1.0)),
                FractalFormulaFactory.createDefault(),
                new RenderProfile(
                        "test",
                        new Resolution(10, 10),
                        new EscapeParameters(64, 4.0),
                        RenderQuality.PREVIEW
                ),
                new ColorProfile(
                        "test",
                        new Palette(List.of(
                                new ColorStop(0.0, new RgbColor(0.0, 0.0, 0.0)),
                                new ColorStop(1.0, new RgbColor(1.0, 1.0, 1.0))
                        ))
                )
        );
    }
}
