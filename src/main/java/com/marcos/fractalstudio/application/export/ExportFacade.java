package com.marcos.fractalstudio.application.export;

import java.io.IOException;

public final class ExportFacade {

    private final ExportFramesUseCase exportFramesUseCase;

    public ExportFacade(ExportFramesUseCase exportFramesUseCase) {
        this.exportFramesUseCase = exportFramesUseCase;
    }

    public ExportResult exportFrames(ExportRequest exportRequest) throws IOException {
        return exportFramesUseCase.export(exportRequest);
    }
}
