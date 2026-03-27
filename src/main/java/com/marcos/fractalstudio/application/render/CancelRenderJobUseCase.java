package com.marcos.fractalstudio.application.render;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;

import java.util.function.Consumer;

public final class CancelRenderJobUseCase {

    private final RenderJobGateway renderJobGateway;

    public CancelRenderJobUseCase(RenderJobGateway renderJobGateway) {
        this.renderJobGateway = renderJobGateway;
    }

    public boolean cancel(String jobId, Consumer<RenderJobStatusDto> statusConsumer) {
        return renderJobGateway.cancel(jobId, statusConsumer);
    }
}
