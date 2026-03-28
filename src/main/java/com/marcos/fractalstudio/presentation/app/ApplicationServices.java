package com.marcos.fractalstudio.presentation.app;

import com.marcos.fractalstudio.application.export.ExportFacade;
import com.marcos.fractalstudio.application.project.ProjectFacade;
import com.marcos.fractalstudio.application.render.RenderFacade;

/**
 * Aggregates the application-layer facades consumed by the desktop shell.
 *
 * <p>Grouping the facades as a single composition result keeps the bootstrap class at the level of
 * "which module is needed" instead of "which constructor argument goes where".
 */
record ApplicationServices(
        ProjectFacade projectFacade,
        RenderFacade renderFacade,
        ExportFacade exportFacade
) {
}
