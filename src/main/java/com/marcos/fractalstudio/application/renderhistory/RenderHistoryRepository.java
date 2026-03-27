package com.marcos.fractalstudio.application.renderhistory;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface RenderHistoryRepository {

    List<RenderJobStatusDto> load(Path historyPath) throws IOException;

    void save(Path historyPath, List<RenderJobStatusDto> statuses) throws IOException;
}
