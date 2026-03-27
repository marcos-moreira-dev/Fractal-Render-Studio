package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectSettings;
import com.marcos.fractalstudio.domain.render.RenderPreset;

public final class UpdateProjectSettingsUseCase {

    public Project update(
            Project project,
            double defaultFramesPerSecond,
            int defaultRenderFrameCount,
            double keyframeStepSeconds,
            RenderPreset defaultRenderPreset
    ) {
        return project.withSettings(new ProjectSettings(
                defaultFramesPerSecond,
                defaultRenderFrameCount,
                keyframeStepSeconds,
                defaultRenderPreset
        ));
    }
}
