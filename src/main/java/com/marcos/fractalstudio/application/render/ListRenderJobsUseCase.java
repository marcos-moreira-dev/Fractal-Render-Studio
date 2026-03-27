package com.marcos.fractalstudio.application.render;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;

import java.util.List;

public final class ListRenderJobsUseCase {

    private final RenderJobGateway renderJobGateway;

    public ListRenderJobsUseCase(RenderJobGateway renderJobGateway) {
        this.renderJobGateway = renderJobGateway;
    }

    public List<RenderJobStatusDto> list() {
        return renderJobGateway.listStatuses();
    }
}
