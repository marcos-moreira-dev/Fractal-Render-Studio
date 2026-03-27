package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.color.ColorProfileFactory;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaFactory;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaType;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.RenderProfile;

public final class UpdateProjectInspectorUseCase {

    public Project update(
            Project project,
            FractalFormulaType fractalFormulaType,
            String colorProfileName,
            int maxIterations,
            double escapeRadius
    ) {
        RenderProfile updatedRenderProfile = new RenderProfile(
                project.renderProfile().name(),
                project.renderProfile().resolution(),
                new EscapeParameters(maxIterations, escapeRadius),
                project.renderProfile().quality()
        );

        return project.withFractalFormula(FractalFormulaFactory.create(fractalFormulaType))
                .withColorProfile(ColorProfileFactory.create(colorProfileName))
                .withRenderProfile(updatedRenderProfile);
    }
}
