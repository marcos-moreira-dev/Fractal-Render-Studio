package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectName;

public final class RenameProjectUseCase {

    public Project rename(Project project, String newProjectName) {
        return project.rename(new ProjectName(newProjectName));
    }
}
