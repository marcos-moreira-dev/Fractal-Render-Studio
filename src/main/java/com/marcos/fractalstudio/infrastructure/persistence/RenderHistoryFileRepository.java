package com.marcos.fractalstudio.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.marcos.fractalstudio.application.dto.RenderJobState;
import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;
import com.marcos.fractalstudio.application.renderhistory.RenderHistoryRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class RenderHistoryFileRepository implements RenderHistoryRepository {

    private static final int HISTORY_LIMIT = 120;

    private final ObjectMapper objectMapper;

    public RenderHistoryFileRepository() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<RenderJobStatusDto> load(Path historyPath) throws IOException {
        if (!Files.exists(historyPath)) {
            return List.of();
        }
        RenderHistoryDocument document = objectMapper.readValue(historyPath.toFile(), RenderHistoryDocument.class);
        if (document.jobs() == null) {
            return List.of();
        }
        return document.jobs().stream()
                .map(job -> new RenderJobStatusDto(
                        job.jobId(),
                        job.jobName(),
                        RenderJobState.valueOf(job.state()),
                        job.completedFrames(),
                        job.totalFrames(),
                        job.progress(),
                        job.message(),
                        job.outputDirectory()
                ))
                .toList();
    }

    @Override
    public void save(Path historyPath, List<RenderJobStatusDto> statuses) throws IOException {
        Files.createDirectories(historyPath.getParent());
        List<RenderJobHistoryDocument> jobs = statuses.stream()
                .limit(HISTORY_LIMIT)
                .map(status -> new RenderJobHistoryDocument(
                        status.jobId(),
                        status.jobName(),
                        status.state().name(),
                        status.completedFrames(),
                        status.totalFrames(),
                        status.progress(),
                        status.message(),
                        status.outputDirectory()
                ))
                .toList();
        objectMapper.writeValue(historyPath.toFile(), new RenderHistoryDocument(jobs));
    }
}
