package com.marcos.fractalstudio.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.marcos.fractalstudio.application.project.ProjectRepository;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.infrastructure.exceptions.ProjectPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON-backed repository for complete project aggregates.
 *
 * <p>The repository persists the full mathematical state required to reopen the
 * product later: formula, points, timeline, metadata, render profile and color
 * profile. It deliberately delegates mapping responsibilities to
 * {@link ProjectSnapshotMapper} so that persistence format and domain model can
 * evolve independently.
 */
public final class ProjectFileRepository implements ProjectRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectFileRepository.class);

    private final ObjectMapper objectMapper;
    private final ProjectSnapshotMapper projectSnapshotMapper = new ProjectSnapshotMapper();

    /**
     * Creates a repository configured for readable JSON output.
     *
     * <p>Indented JSON is useful here because project files are part of the user
     * workflow and may be inspected manually or versioned alongside render
     * artifacts.
     */
    public ProjectFileRepository() {
        objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    /**
     * Persists the given project to the requested path, creating parent
     * directories when necessary.
     */
    public void save(Path projectPath, Project project) throws IOException {
        try {
            Files.createDirectories(projectPath.getParent());
            objectMapper.writeValue(projectPath.toFile(), projectSnapshotMapper.toDocument(project));
            LOGGER.info("Project saved to {}", projectPath.toAbsolutePath());
        } catch (IOException exception) {
            LOGGER.error("Failed to save project to {}", projectPath.toAbsolutePath(), exception);
            throw new ProjectPersistenceException("No se pudo guardar el proyecto JSON.", exception);
        }
    }

    @Override
    /**
     * Loads a project aggregate from a JSON file previously produced by this
     * repository or a compatible legacy snapshot.
     */
    public Project load(Path projectPath) throws IOException {
        try {
            ProjectDocument projectDocument = objectMapper.readValue(projectPath.toFile(), ProjectDocument.class);
            Project project = projectSnapshotMapper.toDomain(projectDocument);
            LOGGER.info("Project loaded from {}", projectPath.toAbsolutePath());
            return project;
        } catch (IOException exception) {
            LOGGER.error("Failed to load project from {}", projectPath.toAbsolutePath(), exception);
            throw new ProjectPersistenceException("No se pudo cargar el proyecto JSON.", exception);
        }
    }
}
