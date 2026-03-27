package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;

public final class UpdateProjectMetadataUseCase {

    public Project updateDescription(Project project, String description) {
        return project.withMetadata(project.metadata().withDescription(description));
    }
}
