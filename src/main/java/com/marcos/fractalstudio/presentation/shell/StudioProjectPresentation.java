package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.application.dto.KeyframeDto;
import com.marcos.fractalstudio.application.project.ProjectFacade;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;
import com.marcos.fractalstudio.infrastructure.rendering.AdaptiveEscapeBudget;

import javafx.scene.control.TreeItem;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Immutable projection of domain project state into labels and tree nodes needed by the shell UI.
 */
final class StudioProjectPresentation {

    private static final DateTimeFormatter SIDEBAR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM HH:mm")
            .withLocale(Locale.forLanguageTag("es-EC"))
            .withZone(ZoneId.systemDefault());

    private final String projectName;
    private final String formulaName;
    private final String colorProfileName;
    private final String renderProfileName;
    private final String resolutionLabel;
    private final String cameraCenterLabel;
    private final String cameraZoomLabel;
    private final String previewIterationsLabel;
    private final String previewModeLabel;
    private final String defaultRenderPresetLabel;
    private final String bookmarkSummary;
    private final String timelineSummary;
    private final String projectDescription;
    private final String projectCreatedAt;
    private final String projectUpdatedAt;
    private final String projectDefaultFps;
    private final List<KeyframeDto> keyframes;
    private final TreeItem<SidebarTreeNode> projectTreeRoot;

    private StudioProjectPresentation(
            String projectName,
            String formulaName,
            String colorProfileName,
            String renderProfileName,
            String resolutionLabel,
            String cameraCenterLabel,
            String cameraZoomLabel,
            String previewIterationsLabel,
            String previewModeLabel,
            String defaultRenderPresetLabel,
            String bookmarkSummary,
            String timelineSummary,
            String projectDescription,
            String projectCreatedAt,
            String projectUpdatedAt,
            String projectDefaultFps,
            List<KeyframeDto> keyframes,
            TreeItem<SidebarTreeNode> projectTreeRoot
    ) {
        this.projectName = projectName;
        this.formulaName = formulaName;
        this.colorProfileName = colorProfileName;
        this.renderProfileName = renderProfileName;
        this.resolutionLabel = resolutionLabel;
        this.cameraCenterLabel = cameraCenterLabel;
        this.cameraZoomLabel = cameraZoomLabel;
        this.previewIterationsLabel = previewIterationsLabel;
        this.previewModeLabel = previewModeLabel;
        this.defaultRenderPresetLabel = defaultRenderPresetLabel;
        this.bookmarkSummary = bookmarkSummary;
        this.timelineSummary = timelineSummary;
        this.projectDescription = projectDescription;
        this.projectCreatedAt = projectCreatedAt;
        this.projectUpdatedAt = projectUpdatedAt;
        this.projectDefaultFps = projectDefaultFps;
        this.keyframes = keyframes;
        this.projectTreeRoot = projectTreeRoot;
    }

    static StudioProjectPresentation from(Project project, CameraState cameraState, ProjectFacade projectFacade) {
        List<KeyframeDto> keyframes = project.timeline().keyframes().stream().map(projectFacade::toKeyframeDto).toList();
        int pointCount = project.bookmarks().size();
        return new StudioProjectPresentation(
                project.name().value(),
                project.fractalFormula().name(),
                project.colorProfile().name(),
                project.renderProfile().name(),
                project.renderProfile().resolution().width() + "x" + project.renderProfile().resolution().height(),
                formatDouble(cameraState.center().x()) + ", " + formatDouble(cameraState.center().y()),
                formatDouble(cameraState.zoomLevel().value()) + "x",
                AdaptiveEscapeBudget.previewIterations(
                        project.renderProfile().escapeParameters().maxIterations(),
                        cameraState.zoomLevel().value()
                ) + " iteraciones",
                "Preview preciso 100%",
                project.settings().defaultRenderPreset().name(),
                pointCount + " puntos",
                pointCount + " puntos",
                project.metadata().description(),
                project.metadata().createdAt().toString(),
                project.metadata().updatedAt().toString(),
                formatDouble(project.settings().defaultFramesPerSecond()) + " fps",
                keyframes,
                buildProjectTree(project)
        );
    }

    String projectName() {
        return projectName;
    }

    String formulaName() {
        return formulaName;
    }

    String colorProfileName() {
        return colorProfileName;
    }

    String renderProfileName() {
        return renderProfileName;
    }

    String resolutionLabel() {
        return resolutionLabel;
    }

    String cameraCenterLabel() {
        return cameraCenterLabel;
    }

    String cameraZoomLabel() {
        return cameraZoomLabel;
    }

    String previewIterationsLabel() {
        return previewIterationsLabel;
    }

    String previewModeLabel() {
        return previewModeLabel;
    }

    String defaultRenderPresetLabel() {
        return defaultRenderPresetLabel;
    }

    String bookmarkSummary() {
        return bookmarkSummary;
    }

    String timelineSummary() {
        return timelineSummary;
    }

    String projectDescription() {
        return projectDescription;
    }

    String projectCreatedAt() {
        return projectCreatedAt;
    }

    String projectUpdatedAt() {
        return projectUpdatedAt;
    }

    String projectDefaultFps() {
        return projectDefaultFps;
    }

    List<KeyframeDto> keyframes() {
        return keyframes;
    }

    TreeItem<SidebarTreeNode> projectTreeRoot() {
        return projectTreeRoot;
    }

    private static TreeItem<SidebarTreeNode> buildProjectTree(Project project) {
        TreeItem<SidebarTreeNode> root = new TreeItem<>(SidebarTreeNode.root(project.name().value()));
        root.setExpanded(true);

        TreeItem<SidebarTreeNode> formulaNode = new TreeItem<>(SidebarTreeNode.info("Formula: " + project.fractalFormula().name()));
        TreeItem<SidebarTreeNode> colorNode = new TreeItem<>(SidebarTreeNode.info("Color: " + project.colorProfile().name()));
        TreeItem<SidebarTreeNode> renderNode = new TreeItem<>(SidebarTreeNode.info(
                "Video: 1920x1080 minimo | " + project.settings().defaultRenderPreset().name()
        ));
        TreeItem<SidebarTreeNode> metadataNode = new TreeItem<>(SidebarTreeNode.section("Metadata"));
        metadataNode.setExpanded(true);
        metadataNode.getChildren().setAll(
                new TreeItem<>(SidebarTreeNode.info("Creado: " + formatSidebarInstant(project.metadata().createdAt()))),
                new TreeItem<>(SidebarTreeNode.info("Actualizado: " + formatSidebarInstant(project.metadata().updatedAt()))),
                new TreeItem<>(SidebarTreeNode.info("FPS: " + formatDouble(project.settings().defaultFramesPerSecond())))
        );
        TreeItem<SidebarTreeNode> bookmarksNode = new TreeItem<>(SidebarTreeNode.section("Puntos"));
        bookmarksNode.setExpanded(true);
        for (ProjectBookmark bookmark : project.bookmarks()) {
            bookmarksNode.getChildren().add(new TreeItem<>(SidebarTreeNode.bookmark(
                    bookmark.label() + "  |  " + formatDouble(bookmark.cameraState().zoomLevel().value()) + "x",
                    bookmark.id().value()
            )));
        }

        root.getChildren().setAll(formulaNode, colorNode, renderNode, metadataNode, bookmarksNode);
        return root;
    }

    private static String formatDouble(double value) {
        return String.format(Locale.US, "%.4f", value);
    }

    private static String formatSidebarInstant(Instant instant) {
        return SIDEBAR_DATE_FORMATTER.format(instant);
    }
}
