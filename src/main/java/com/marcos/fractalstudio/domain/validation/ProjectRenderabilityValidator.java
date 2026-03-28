package com.marcos.fractalstudio.domain.validation;

import com.marcos.fractalstudio.domain.exceptions.ProjectRenderabilityException;
import com.marcos.fractalstudio.domain.project.Project;

/**
 * Evaluates whether a project satisfies the minimum rules required for final rendering.
 *
 * <p>The validator keeps renderability rules outside the aggregate constructor so that project
 * instances can exist during intermediate editing states while still failing explicitly when the user
 * attempts to generate preview or video artifacts that require stricter guarantees.
 */
public final class ProjectRenderabilityValidator {

    private final TimelineConsistencyValidator timelineConsistencyValidator = new TimelineConsistencyValidator();

    /**
     * Validates renderability and raises a domain-specific exception when any invariant is broken.
     *
     * @param project project aggregate to validate
     * @throws ProjectRenderabilityException when the project cannot be rendered safely
     */
    public void validate(Project project) {
        timelineConsistencyValidator.validate(project.timeline());
        if (project.renderProfile().resolution().width() < 64 || project.renderProfile().resolution().height() < 64) {
            throw new ProjectRenderabilityException("Render resolution is too small for a production render.");
        }
        if (project.renderProfile().escapeParameters().maxIterations() < 32) {
            throw new ProjectRenderabilityException("Render profile requires at least 32 iterations.");
        }
    }
}
