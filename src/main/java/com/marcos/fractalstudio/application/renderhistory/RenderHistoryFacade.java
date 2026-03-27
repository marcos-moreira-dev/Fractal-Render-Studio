package com.marcos.fractalstudio.application.renderhistory;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class RenderHistoryFacade {

    private final LoadRenderHistoryUseCase loadRenderHistoryUseCase;
    private final SaveRenderHistoryUseCase saveRenderHistoryUseCase;

    public RenderHistoryFacade(
            LoadRenderHistoryUseCase loadRenderHistoryUseCase,
            SaveRenderHistoryUseCase saveRenderHistoryUseCase
    ) {
        this.loadRenderHistoryUseCase = loadRenderHistoryUseCase;
        this.saveRenderHistoryUseCase = saveRenderHistoryUseCase;
    }

    public List<RenderJobStatusDto> load(Path historyPath) throws IOException {
        return loadRenderHistoryUseCase.load(historyPath);
    }

    public void save(Path historyPath, List<RenderJobStatusDto> statuses) throws IOException {
        saveRenderHistoryUseCase.save(historyPath, statuses);
    }
}
