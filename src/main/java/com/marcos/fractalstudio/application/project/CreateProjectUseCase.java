package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.color.ColorProfileFactory;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaFactory;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectId;
import com.marcos.fractalstudio.domain.project.ProjectMetadata;
import com.marcos.fractalstudio.domain.project.ProjectName;
import com.marcos.fractalstudio.domain.project.ProjectSettings;
import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.RenderPreset;
import com.marcos.fractalstudio.domain.render.RenderProfile;
import com.marcos.fractalstudio.domain.render.RenderQuality;
import com.marcos.fractalstudio.domain.render.Resolution;
import com.marcos.fractalstudio.domain.timeline.Timeline;
public final class CreateProjectUseCase {

    public Project create(String projectName) {
        return new Project(
                ProjectId.create(),
                new ProjectName(projectName),
                FractalFormulaFactory.createDefault(),
                new Timeline(),
                new RenderProfile(
                        "Preview/Final Default",
                        new Resolution(1920, 1080),
                        new EscapeParameters(128, 4.0),
                        RenderQuality.FINAL
                ),
                ColorProfileFactory.createDefault(),
                ProjectMetadata.create("Fractal exploration project."),
                new ProjectSettings(6.0, 24, 2.0, RenderPreset.STANDARD),
                java.util.List.of()
        );
    }
}
