package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.application.preview.DeepZoomAdvisory;
import com.marcos.fractalstudio.application.preview.MemoryPressureSnapshot;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.infrastructure.rendering.AdaptiveEscapeBudget;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Holds the observable labels and editable numeric fields shown by the inspector.
 *
 * <p>The shell view model needs to coordinate project mutations and preview requests, but it does not
 * need to own every label property used by the right-hand inspector. This state object isolates the
 * purely presentational projection of project, camera and preview diagnostics.
 */
final class StudioInspectorState {

    private final StringProperty projectName = new SimpleStringProperty();
    private final StringProperty formulaName = new SimpleStringProperty();
    private final StringProperty colorProfileName = new SimpleStringProperty();
    private final StringProperty renderProfileName = new SimpleStringProperty();
    private final StringProperty resolutionLabel = new SimpleStringProperty();
    private final StringProperty cameraCenterLabel = new SimpleStringProperty();
    private final StringProperty cameraZoomLabel = new SimpleStringProperty();
    private final StringProperty previewIterationsLabel = new SimpleStringProperty();
    private final StringProperty previewModeLabel = new SimpleStringProperty();
    private final StringProperty deepZoomHealthLabel = new SimpleStringProperty();
    private final StringProperty deepZoomMemoryLabel = new SimpleStringProperty();
    private final StringProperty defaultRenderPresetLabel = new SimpleStringProperty();
    private final StringProperty bookmarkSummary = new SimpleStringProperty();
    private final SimpleIntegerProperty configuredMaxIterations = new SimpleIntegerProperty();
    private final SimpleDoubleProperty configuredEscapeRadius = new SimpleDoubleProperty();
    private final StringProperty timelineSummary = new SimpleStringProperty();
    private final StringProperty projectDescription = new SimpleStringProperty();
    private final StringProperty projectCreatedAt = new SimpleStringProperty();
    private final StringProperty projectUpdatedAt = new SimpleStringProperty();
    private final StringProperty projectDefaultFps = new SimpleStringProperty();

    void applyProjectPresentation(StudioProjectPresentation presentation, Project project, MemoryPressureSnapshot memoryPressure) {
        projectName.set(presentation.projectName());
        formulaName.set(presentation.formulaName());
        colorProfileName.set(presentation.colorProfileName());
        renderProfileName.set(presentation.renderProfileName());
        resolutionLabel.set(presentation.resolutionLabel());
        cameraCenterLabel.set(presentation.cameraCenterLabel());
        cameraZoomLabel.set(presentation.cameraZoomLabel());
        previewIterationsLabel.set(presentation.previewIterationsLabel());
        previewModeLabel.set(presentation.previewModeLabel());
        deepZoomHealthLabel.set("Estable");
        deepZoomMemoryLabel.set(memoryPressure.compactLabel());
        defaultRenderPresetLabel.set(presentation.defaultRenderPresetLabel());
        bookmarkSummary.set(presentation.bookmarkSummary());
        configuredMaxIterations.set(project.renderProfile().escapeParameters().maxIterations());
        configuredEscapeRadius.set(project.renderProfile().escapeParameters().escapeRadius());
        timelineSummary.set(presentation.timelineSummary());
        projectDescription.set(presentation.projectDescription());
        projectCreatedAt.set(presentation.projectCreatedAt());
        projectUpdatedAt.set(presentation.projectUpdatedAt());
        projectDefaultFps.set(presentation.projectDefaultFps());
    }

    void refreshCameraState(CameraState cameraState, Project project) {
        cameraCenterLabel.set(formatDouble(cameraState.center().x()) + ", " + formatDouble(cameraState.center().y()));
        cameraZoomLabel.set(formatDouble(cameraState.zoomLevel().value()) + "x");
        previewIterationsLabel.set(AdaptiveEscapeBudget.previewIterations(
                project.renderProfile().escapeParameters().maxIterations(),
                cameraState.zoomLevel().value()
        ) + " iteraciones");
    }

    void applyPreviewPlan(String statusLabel, int maxIterations) {
        previewModeLabel.set(statusLabel);
        previewIterationsLabel.set(maxIterations + " iteraciones");
    }

    void applyDeepZoomAdvisory(DeepZoomAdvisory advisory) {
        deepZoomHealthLabel.set(advisory.healthLabel());
        deepZoomMemoryLabel.set(advisory.memoryLabel());
    }

    void resetPreviewState(MemoryPressureSnapshot memoryPressure) {
        previewModeLabel.set("Preview preciso 100%");
        deepZoomHealthLabel.set("Estable");
        deepZoomMemoryLabel.set(memoryPressure.compactLabel());
    }

    ReadOnlyStringProperty projectNameProperty() {
        return projectName;
    }

    ReadOnlyStringProperty formulaNameProperty() {
        return formulaName;
    }

    ReadOnlyStringProperty colorProfileNameProperty() {
        return colorProfileName;
    }

    ReadOnlyStringProperty renderProfileNameProperty() {
        return renderProfileName;
    }

    ReadOnlyStringProperty resolutionLabelProperty() {
        return resolutionLabel;
    }

    ReadOnlyStringProperty cameraCenterLabelProperty() {
        return cameraCenterLabel;
    }

    ReadOnlyStringProperty cameraZoomLabelProperty() {
        return cameraZoomLabel;
    }

    ReadOnlyStringProperty previewIterationsLabelProperty() {
        return previewIterationsLabel;
    }

    ReadOnlyStringProperty previewModeLabelProperty() {
        return previewModeLabel;
    }

    ReadOnlyStringProperty deepZoomHealthLabelProperty() {
        return deepZoomHealthLabel;
    }

    ReadOnlyStringProperty deepZoomMemoryLabelProperty() {
        return deepZoomMemoryLabel;
    }

    ReadOnlyIntegerProperty configuredMaxIterationsProperty() {
        return configuredMaxIterations;
    }

    ReadOnlyDoubleProperty configuredEscapeRadiusProperty() {
        return configuredEscapeRadius;
    }

    ReadOnlyStringProperty defaultRenderPresetLabelProperty() {
        return defaultRenderPresetLabel;
    }

    ReadOnlyStringProperty bookmarkSummaryProperty() {
        return bookmarkSummary;
    }

    ReadOnlyStringProperty timelineSummaryProperty() {
        return timelineSummary;
    }

    ReadOnlyStringProperty projectDescriptionProperty() {
        return projectDescription;
    }

    ReadOnlyStringProperty projectCreatedAtProperty() {
        return projectCreatedAt;
    }

    ReadOnlyStringProperty projectUpdatedAtProperty() {
        return projectUpdatedAt;
    }

    ReadOnlyStringProperty projectDefaultFpsProperty() {
        return projectDefaultFps;
    }

    private String formatDouble(double value) {
        return String.format("%.4f", value);
    }
}
