package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;

import java.io.IOException;
import java.nio.file.Path;

public final class SaveProjectUseCase {

    private final ProjectRepository projectRepository;

    public SaveProjectUseCase(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void save(Project project, Path projectPath) throws IOException {
        projectRepository.save(projectPath, project);
    }
}
