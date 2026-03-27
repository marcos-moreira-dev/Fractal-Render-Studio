package com.marcos.fractalstudio.infrastructure.batching;

import com.marcos.fractalstudio.application.dto.RenderJobState;
import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;
import com.marcos.fractalstudio.application.render.RenderJobGateway;
import com.marcos.fractalstudio.application.render.RenderPlan;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;
import com.marcos.fractalstudio.infrastructure.export.FrameSequenceExporter;
import com.marcos.fractalstudio.infrastructure.export.SequenceVideoExporter;
import com.marcos.fractalstudio.infrastructure.metrics.BatchMetricsCollector;
import com.marcos.fractalstudio.infrastructure.rendering.FrameRendererFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public final class WorkerPoolManager implements RenderJobGateway {

    private final FrameRendererFactory frameRendererFactory;
    private final FrameSequenceExporter frameSequenceExporter;
    private final SequenceVideoExporter sequenceVideoExporter;
    private final ExecutorService renderExecutorService;
    private final InMemoryRenderQueue inMemoryRenderQueue;
    private final BatchMetricsCollector batchMetricsCollector;

    public WorkerPoolManager(
            FrameRendererFactory frameRendererFactory,
            FrameSequenceExporter frameSequenceExporter,
            SequenceVideoExporter sequenceVideoExporter,
            ExecutorService renderExecutorService,
            InMemoryRenderQueue inMemoryRenderQueue,
            BatchMetricsCollector batchMetricsCollector
    ) {
        this.frameRendererFactory = frameRendererFactory;
        this.frameSequenceExporter = frameSequenceExporter;
        this.sequenceVideoExporter = sequenceVideoExporter;
        this.renderExecutorService = renderExecutorService;
        this.inMemoryRenderQueue = inMemoryRenderQueue;
        this.batchMetricsCollector = batchMetricsCollector;
    }

    @Override
    public void submit(RenderPlan renderPlan, Consumer<RenderJobStatusDto> statusConsumer) {
        JobCancellationToken cancellationToken = new JobCancellationToken();
        RenderJob renderJob = new RenderJob(
                RenderJobId.create(),
                renderPlan.jobName(),
                renderPlan.outputDirectory(),
                renderPlan.frameDescriptors().size(),
                cancellationToken
        );
        inMemoryRenderQueue.register(renderJob);
        int totalFrames = renderPlan.frameDescriptors().size();

        statusConsumer.accept(toStatusDto(renderJob));

        renderExecutorService.submit(() -> {
            try {
                Path framesDirectory = renderPlan.outputDirectory().resolve("frames");
                if (renderJob.cancellationToken().isCancellationRequested()) {
                    renderJob.markCancelled("Render cancelado antes de iniciar");
                    statusConsumer.accept(toStatusDto(renderJob));
                    return;
                }
                renderJob.markPreparing("Preparando secuencia");
                statusConsumer.accept(toStatusDto(renderJob));

                int completedFrames = 0;
                for (FrameDescriptor frameDescriptor : renderPlan.frameDescriptors()) {
                    if (renderJob.cancellationToken().isCancellationRequested()) {
                        renderJob.markCancelled("Render cancelado por el usuario");
                        statusConsumer.accept(toStatusDto(renderJob));
                        return;
                    }
                    RenderedFrame renderedFrame = frameRendererFactory.create(frameDescriptor.renderProfile().quality()).render(frameDescriptor);
                    if (renderJob.cancellationToken().isCancellationRequested()) {
                        renderJob.markCancelled("Render cancelado por el usuario");
                        statusConsumer.accept(toStatusDto(renderJob));
                        return;
                    }
                    frameSequenceExporter.exportFrame(framesDirectory, frameDescriptor, renderedFrame);
                    completedFrames++;
                    renderJob.markRendering(completedFrames, "Renderizando frame " + completedFrames + " de " + totalFrames);
                    statusConsumer.accept(toStatusDto(renderJob));
                }

                renderJob.markPreparing("Codificando video MP4");
                statusConsumer.accept(toStatusDto(renderJob));
                Path videoPath = renderPlan.outputDirectory().resolve("render.mp4");
                sequenceVideoExporter.exportVideo(framesDirectory, videoPath, renderPlan.framesPerSecond());

                renderJob.markCompleted("Video MP4 listo: " + videoPath.getFileName());
                statusConsumer.accept(new RenderJobStatusDto(
                        renderJob.id().value(),
                        renderJob.name(),
                        RenderJobState.COMPLETED,
                        totalFrames,
                        totalFrames,
                        1.0,
                        "Video MP4 listo en " + batchMetricsCollector.measure(renderJob.startedAt(), renderJob.finishedAt()).toSeconds() + "s",
                        renderJob.outputDirectory().toString()
                ));
            } catch (Exception exception) {
                renderJob.markFailed(exception.getMessage());
                statusConsumer.accept(new RenderJobStatusDto(
                        renderJob.id().value(),
                        renderJob.name(),
                        RenderJobState.FAILED,
                        renderJob.completedFrames(),
                        totalFrames,
                        renderJob.progress(),
                        exception.getMessage(),
                        renderJob.outputDirectory().toString()
                ));
            }
        });
    }

    @Override
    public boolean cancel(String jobId, Consumer<RenderJobStatusDto> statusConsumer) {
        RenderJob renderJob = inMemoryRenderQueue.find(jobId);
        if (renderJob == null) {
            return false;
        }
        if (renderJob.state() == JobState.COMPLETED || renderJob.state() == JobState.FAILED || renderJob.state() == JobState.CANCELLED) {
            statusConsumer.accept(toStatusDto(renderJob));
            return false;
        }
        renderJob.cancellationToken().cancel();
        renderJob.markCancellationRequested("Cancelacion solicitada");
        statusConsumer.accept(toStatusDto(renderJob));
        return true;
    }

    @Override
    public List<RenderJobStatusDto> listStatuses() {
        return inMemoryRenderQueue.jobs().stream()
                .map(this::toStatusDto)
                .toList();
    }

    private RenderJobStatusDto toStatusDto(RenderJob renderJob) {
        return new RenderJobStatusDto(
                renderJob.id().value(),
                renderJob.name(),
                RenderJobState.valueOf(renderJob.state().name()),
                renderJob.completedFrames(),
                renderJob.totalFrames(),
                renderJob.progress(),
                renderJob.message(),
                renderJob.outputDirectory().toString()
        );
    }
}
