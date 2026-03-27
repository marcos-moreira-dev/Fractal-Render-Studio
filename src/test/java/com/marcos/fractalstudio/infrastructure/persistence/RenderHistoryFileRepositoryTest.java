package com.marcos.fractalstudio.infrastructure.persistence;

import com.marcos.fractalstudio.application.dto.RenderJobState;
import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RenderHistoryFileRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsRenderHistory() throws IOException {
        RenderHistoryFileRepository repository = new RenderHistoryFileRepository();
        Path historyPath = tempDir.resolve("renders").resolve("render-history.json");
        List<RenderJobStatusDto> statuses = List.of(
                new RenderJobStatusDto("job-01", "Preview Batch", RenderJobState.COMPLETED, 24, 24, 1.0, "ok", "storage/renders/job-01"),
                new RenderJobStatusDto("job-02", "Deep Zoom", RenderJobState.FAILED, 7, 24, 0.29, "boom", "storage/renders/job-02")
        );

        repository.save(historyPath, statuses);
        List<RenderJobStatusDto> restored = repository.load(historyPath);

        assertEquals(statuses, restored);
    }
}
