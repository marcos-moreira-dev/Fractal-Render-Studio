package com.marcos.fractalstudio.application.renderhistory;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class SaveRenderHistoryUseCase {

    private final RenderHistoryRepository renderHistoryRepository;

    public SaveRenderHistoryUseCase(RenderHistoryRepository renderHistoryRepository) {
        this.renderHistoryRepository = renderHistoryRepository;
    }

    public void save(Path historyPath, List<RenderJobStatusDto> statuses) throws IOException {
        renderHistoryRepository.save(historyPath, statuses);
    }
}
