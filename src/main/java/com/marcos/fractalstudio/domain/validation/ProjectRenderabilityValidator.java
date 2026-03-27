package com.marcos.fractalstudio.domain.validation;

import com.marcos.fractalstudio.domain.project.Project;

public final class ProjectRenderabilityValidator {

    private final TimelineConsistencyValidator timelineConsistencyValidator = new TimelineConsistencyValidator();

    public void validate(Project project) {
        timelineConsistencyValidator.validate(project.timeline());
        if (project.renderProfile().resolution().width() < 64 || project.renderProfile().resolution().height() < 64) {
            throw new IllegalStateException("Render resolution is too small for a production render.");
        }
        if (project.renderProfile().escapeParameters().maxIterations() < 32) {
            throw new IllegalStateException("Render profile requires at least 32 iterations.");
        }
    }
}
