package com.marcos.fractalstudio.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.marcos.fractalstudio.application.project.ProjectRepository;
import com.marcos.fractalstudio.domain.project.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectFileRepository implements ProjectRepository {

    private final ObjectMapper objectMapper;
    private final ProjectSnapshotMapper projectSnapshotMapper = new ProjectSnapshotMapper();

    public ProjectFileRepository() {
        objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void save(Path projectPath, Project project) throws IOException {
        Files.createDirectories(projectPath.getParent());
        objectMapper.writeValue(projectPath.toFile(), projectSnapshotMapper.toDocument(project));
    }

    @Override
    public Project load(Path projectPath) throws IOException {
        ProjectDocument projectDocument = objectMapper.readValue(projectPath.toFile(), ProjectDocument.class);
        return projectSnapshotMapper.toDomain(projectDocument);
    }
}
