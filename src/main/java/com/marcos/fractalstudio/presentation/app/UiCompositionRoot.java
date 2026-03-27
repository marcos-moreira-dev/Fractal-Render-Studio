package com.marcos.fractalstudio.presentation.app;

import com.marcos.fractalstudio.presentation.metrics.MetricsScreenView;
import com.marcos.fractalstudio.presentation.navigation.Route;
import com.marcos.fractalstudio.presentation.navigation.RouteSpec;
import com.marcos.fractalstudio.presentation.navigation.StudioNavigator;
import com.marcos.fractalstudio.presentation.navigation.ViewRegistry;
import com.marcos.fractalstudio.presentation.shell.StudioShellView;
import com.marcos.fractalstudio.presentation.shell.StudioShellViewModel;
import com.marcos.fractalstudio.presentation.shell.StudioWorkspaceView;

import javafx.scene.Parent;

/**
 * Wires the top-level navigation shell and screen registry.
 */
public final class UiCompositionRoot {

    /**
     * Creates the root node for the desktop application.
     *
     * @param studioShellViewModel shared shell state
     * @return top-level JavaFX node
     */
    public Parent compose(StudioShellViewModel studioShellViewModel) {
        StudioNavigator studioNavigator = new StudioNavigator();
        ViewRegistry viewRegistry = new ViewRegistry();
        viewRegistry.register(new RouteSpec(Route.STUDIO, "Studio Workspace", "Studio"), () -> new StudioWorkspaceView(studioShellViewModel));
        viewRegistry.register(new RouteSpec(Route.METRICS, "Observabilidad", "Metrics"), () -> new MetricsScreenView(studioShellViewModel));
        return new StudioShellView(studioShellViewModel, studioNavigator, viewRegistry).root();
    }
}
