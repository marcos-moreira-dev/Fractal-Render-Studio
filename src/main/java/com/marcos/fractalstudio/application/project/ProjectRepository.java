package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;

import java.io.IOException;
import java.nio.file.Path;

public interface ProjectRepository {

    void save(Path projectPath, Project project) throws IOException;

    Project load(Path projectPath) throws IOException;
}
