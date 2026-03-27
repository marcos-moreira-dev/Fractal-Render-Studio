package com.marcos.fractalstudio.application.render;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;
import com.marcos.fractalstudio.application.timeline.BuildTimelineSequenceUseCase;

import java.util.function.Consumer;

public final class SubmitRenderJobUseCase {

    private final BuildTimelineSequenceUseCase buildTimelineSequenceUseCase;
    private final RenderJobGateway renderJobGateway;

    public SubmitRenderJobUseCase(
            BuildTimelineSequenceUseCase buildTimelineSequenceUseCase,
            RenderJobGateway renderJobGateway
    ) {
        this.buildTimelineSequenceUseCase = buildTimelineSequenceUseCase;
        this.renderJobGateway = renderJobGateway;
    }

    public void submit(RenderRequest renderRequest, Consumer<RenderJobStatusDto> statusConsumer) {
        RenderPlan renderPlan = new RenderPlan(
                renderRequest.jobName(),
                renderRequest.outputDirectory(),
                renderRequest.framesPerSecond(),
                buildTimelineSequenceUseCase.build(
                        renderRequest.project(),
                        renderRequest.fallbackCamera(),
                        renderRequest.totalFrames(),
                        renderRequest.framesPerSecond(),
                        renderRequest.renderPreset()
                )
        );
        renderJobGateway.submit(renderPlan, statusConsumer);
    }
}
