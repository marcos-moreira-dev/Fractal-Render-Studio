package com.marcos.fractalstudio.application.renderhistory;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class LoadRenderHistoryUseCase {

    private final RenderHistoryRepository renderHistoryRepository;

    public LoadRenderHistoryUseCase(RenderHistoryRepository renderHistoryRepository) {
        this.renderHistoryRepository = renderHistoryRepository;
    }

    public List<RenderJobStatusDto> load(Path historyPath) throws IOException {
        return renderHistoryRepository.load(historyPath);
    }
}
