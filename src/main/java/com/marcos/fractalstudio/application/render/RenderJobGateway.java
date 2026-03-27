package com.marcos.fractalstudio.application.render;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;

import java.util.List;
import java.util.function.Consumer;

public interface RenderJobGateway {

    void submit(RenderPlan renderPlan, Consumer<RenderJobStatusDto> statusConsumer);

    boolean cancel(String jobId, Consumer<RenderJobStatusDto> statusConsumer);

    List<RenderJobStatusDto> listStatuses();
}
