package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.presentation.explorer.FractalExplorerView;
import com.marcos.fractalstudio.presentation.inspector.InspectorView;
import com.marcos.fractalstudio.presentation.metrics.MetricsView;
import com.marcos.fractalstudio.presentation.renderqueue.RenderQueueView;
import com.marcos.fractalstudio.presentation.timeline.TimelineView;

import com.marcos.fractalstudio.presentation.dialogs.ProjectSettingsDialog;
import com.marcos.fractalstudio.presentation.dialogs.DialogStyler;
import com.marcos.fractalstudio.presentation.dialogs.RenderSettingsDialog;
import com.marcos.fractalstudio.presentation.navigation.Route;
import com.marcos.fractalstudio.presentation.navigation.StudioNavigator;
import com.marcos.fractalstudio.presentation.navigation.ViewRegistry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Path;

public final class StudioShellView {

    private final BorderPane root;
    private final StackPane contentHost = new StackPane();

    public StudioShellView(StudioShellViewModel viewModel, StudioNavigator navigator, ViewRegistry viewRegistry) {
        root = new BorderPane();
        root.getStyleClass().add("studio-root");

        ToolBar toolBar = buildToolBar(viewModel, navigator, viewRegistry);
        root.setTop(toolBar);
        root.setCenter(contentHost);
        viewModel.pendingDeepZoomAdvisoryProperty().addListener((ignored, oldValue, newValue) -> {
            if (newValue == null || root.getScene() == null || root.getScene().getWindow() == null) {
                return;
            }
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Deep Zoom");
            alert.setHeaderText(newValue.headline());
            alert.setContentText(newValue.message());
            alert.initOwner(root.getScene().getWindow());
            DialogStyler.apply(alert);
            alert.showAndWait();
            viewModel.clearPendingDeepZoomAdvisory();
        });
        navigator.activeRouteProperty().addListener((ignored, oldRoute, newRoute) -> showRoute(newRoute, viewRegistry));
        showRoute(navigator.activeRoute(), viewRegistry);
    }

    public Parent root() {
        return root;
    }

    private ToolBar buildToolBar(StudioShellViewModel viewModel, StudioNavigator navigator, ViewRegistry viewRegistry) {
        MenuItem newProjectItem = buildToolbarMenuItem(
                "Nuevo proyecto",
                "bi-file-earmark-plus",
                "Crea un proyecto limpio y reinicia la sesion actual.",
                viewModel::createNewProject
        );
        MenuItem loadItem = buildToolbarMenuItem(
                "Abrir proyecto...",
                "bi-folder2-open",
                "Abre un archivo de proyecto guardado manualmente.",
                () -> chooseProjectFile(root.getScene() == null ? null : root.getScene().getWindow(), false)
                        .ifPresent(path -> viewModel.loadProjectFrom(path, true))
        );
        MenuItem saveItem = buildToolbarMenuItem(
                "Guardar proyecto...",
                "bi-save",
                "Exporta el proyecto actual a un archivo abierto luego por la aplicacion.",
                () -> chooseProjectFile(root.getScene() == null ? null : root.getScene().getWindow(), true)
                        .ifPresent(viewModel::saveProjectTo)
        );

        MenuButton fileMenuButton = buildToolbarMenu(
                "Archivo",
                "bi-folder2-open",
                "Acciones basicas de proyecto: crear, abrir y guardar.",
                newProjectItem,
                loadItem,
                saveItem
        );

        MenuItem projectSettingsItem = buildToolbarMenuItem(
                "Ajustes del proyecto",
                "bi-sliders",
                "Configura nombre, descripcion, duracion y FPS por defecto.",
                () -> new ProjectSettingsDialog().show(
                root.getScene().getWindow(),
                viewModel.currentProjectName(),
                viewModel.currentProjectDescription(),
                viewModel.currentDefaultFramesPerSecond(),
                viewModel.currentDefaultRenderFrameCount(),
                viewModel.currentKeyframeStepSeconds(),
                viewModel.currentDefaultRenderPreset()
        ).ifPresent(result -> viewModel.applyProjectSettings(
                result.projectName(),
                result.description(),
                result.defaultFramesPerSecond(),
                result.defaultDurationSeconds(),
                result.keyframeStepSeconds(),
                result.defaultRenderPreset()
        ))
        );

        MenuButton projectMenuButton = buildToolbarMenu(
                "Proyecto",
                "bi-gear",
                "Ajustes generales y configuracion del proyecto actual.",
                projectSettingsItem
        );

        Button renderButton = buildToolbarButton(
                "Render video",
                "bi-camera-video",
                "Abre el dialogo de render para generar el video MP4 final.",
                () -> new RenderSettingsDialog().show(
                root.getScene().getWindow(),
                viewModel.currentSuggestedRenderWorkspaceName(),
                viewModel.currentSuggestedRenderDurationSeconds(),
                viewModel.currentDefaultFramesPerSecond(),
                viewModel.defaultRenderWorkspaceBaseDirectory().toString()
        ).ifPresent(result -> {
            String baseDirectory = result.baseDirectory() == null || result.baseDirectory().isBlank()
                    ? viewModel.defaultRenderWorkspaceBaseDirectory().toString()
                    : result.baseDirectory().trim();
            viewModel.submitRender(
                    result.renderName(),
                    viewModel.estimateRenderFrameCount(result.durationSeconds(), result.framesPerSecond()),
                    result.framesPerSecond(),
                    java.nio.file.Path.of(baseDirectory),
                    viewModel.currentDefaultRenderPreset()
            );
        })
        );

        Button studioRouteButton = buildToolbarButton(
                viewRegistry.routeSpec(Route.STUDIO).buttonLabel(),
                "bi-grid-1x2",
                "Abre el espacio principal de exploracion y edicion.",
                () -> navigator.navigate(Route.STUDIO)
        );
        studioRouteButton.getStyleClass().add("toolbar-route-button");

        Button metricsRouteButton = buildToolbarButton(
                viewRegistry.routeSpec(Route.METRICS).buttonLabel(),
                "bi-activity",
                "Muestra metricas y registro interno de la sesion.",
                () -> navigator.navigate(Route.METRICS)
        );
        metricsRouteButton.getStyleClass().add("toolbar-route-button");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(24.0);
        spacer.setMaxWidth(Double.MAX_VALUE);

        Label titleLabel = new Label();
        titleLabel.textProperty().bind(viewModel.projectNameProperty());
        titleLabel.getStyleClass().add("toolbar-title");
        titleLabel.setMinWidth(220.0);
        titleLabel.setMaxWidth(420.0);
        titleLabel.setAlignment(Pos.CENTER_RIGHT);

        ToolBar toolBar = new ToolBar(
                studioRouteButton,
                metricsRouteButton,
                new Separator(),
                fileMenuButton,
                projectMenuButton,
                renderButton,
                spacer,
                titleLabel
        );
        toolBar.getStyleClass().add("shell-toolbar");
        toolBar.setPadding(new Insets(8.0));
        navigator.activeRouteProperty().addListener((ignored, oldRoute, newRoute) ->
                updateRouteButtons(newRoute, studioRouteButton, metricsRouteButton));
        updateRouteButtons(navigator.activeRoute(), studioRouteButton, metricsRouteButton);
        return toolBar;
    }

    private void showRoute(Route route, ViewRegistry viewRegistry) {
        contentHost.getChildren().setAll(viewRegistry.create(route));
    }

    private void updateRouteButtons(Route activeRoute, Button studioRouteButton, Button metricsRouteButton) {
        setRouteButtonState(studioRouteButton, activeRoute == Route.STUDIO);
        setRouteButtonState(metricsRouteButton, activeRoute == Route.METRICS);
    }

    private void setRouteButtonState(Button button, boolean active) {
        if (active) {
            if (!button.getStyleClass().contains("toolbar-route-button-active")) {
                button.getStyleClass().add("toolbar-route-button-active");
            }
        } else {
            button.getStyleClass().remove("toolbar-route-button-active");
        }
    }

    private Button buildToolbarButton(String text, String iconLiteral, String tooltipText, Runnable action) {
        Button button = new Button(text);
        button.setGraphic(createToolbarIcon(iconLiteral));
        button.getStyleClass().add("toolbar-action-button");
        button.setTooltip(new Tooltip(tooltipText));
        button.setOnAction(event -> action.run());
        return button;
    }

    private MenuButton buildToolbarMenu(String text, String iconLiteral, String tooltipText, MenuItem... items) {
        MenuButton menuButton = new MenuButton(text);
        menuButton.setGraphic(createToolbarIcon(iconLiteral));
        menuButton.getItems().setAll(items);
        menuButton.getStyleClass().add("toolbar-action-button");
        menuButton.setTooltip(new Tooltip(tooltipText));
        return menuButton;
    }

    private MenuItem buildToolbarMenuItem(String text, String iconLiteral, String tooltipText, Runnable action) {
        MenuItem menuItem = new MenuItem(text, createToolbarIcon(iconLiteral));
        menuItem.setOnAction(event -> action.run());
        return menuItem;
    }

    private Node createToolbarIcon(String iconLiteral) {
        try {
            FontIcon icon = new FontIcon(iconLiteral);
            icon.getStyleClass().add("toolbar-icon");
            icon.setIconSize(14);
            return icon;
        } catch (IllegalArgumentException exception) {
            Label fallback = new Label("•");
            fallback.getStyleClass().add("toolbar-icon-fallback");
            return fallback;
        }
    }

    private java.util.Optional<Path> chooseProjectFile(javafx.stage.Window owner, boolean saving) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(saving ? "Guardar proyecto Fractal Studio" : "Abrir proyecto Fractal Studio");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Proyecto Fractal Studio", "*.fractalstudio.json"));
        chooser.setInitialFileName("fractal-studio.fractalstudio.json");
        File selected = saving ? chooser.showSaveDialog(owner) : chooser.showOpenDialog(owner);
        return selected == null ? java.util.Optional.empty() : java.util.Optional.of(selected.toPath());
    }
}
