package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;

import java.io.IOException;
import java.nio.file.Path;

public final class LoadProjectUseCase {

    private final ProjectRepository projectRepository;

    public LoadProjectUseCase(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project load(Path projectPath) throws IOException {
        return projectRepository.load(projectPath);
    }
}
