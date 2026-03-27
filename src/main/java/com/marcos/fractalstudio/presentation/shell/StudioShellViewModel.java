package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.application.dto.KeyframeDto;
import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;
import com.marcos.fractalstudio.application.export.ExportFacade;
import com.marcos.fractalstudio.application.export.ExportRequest;
import com.marcos.fractalstudio.application.export.ExportResult;
import com.marcos.fractalstudio.application.preview.AdaptivePreviewQualityPolicy;
import com.marcos.fractalstudio.application.preview.DeepZoomAdvisor;
import com.marcos.fractalstudio.application.preview.DeepZoomAdvisory;
import com.marcos.fractalstudio.application.preview.MemoryPressureSnapshot;
import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.application.preview.ZoomLimitPolicy;
import com.marcos.fractalstudio.application.project.ProjectFacade;
import com.marcos.fractalstudio.application.render.RenderFacade;
import com.marcos.fractalstudio.application.render.RenderRequest;
import com.marcos.fractalstudio.application.renderhistory.RenderHistoryFacade;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;
import com.marcos.fractalstudio.domain.color.ColorProfileFactory;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaFactory;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaType;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;
import com.marcos.fractalstudio.domain.project.ProjectBookmarkId;
import com.marcos.fractalstudio.domain.render.RenderPreset;
import com.marcos.fractalstudio.domain.timeline.Keyframe;
import com.marcos.fractalstudio.domain.timeline.KeyframeId;
import com.marcos.fractalstudio.domain.timeline.TimePosition;
import com.marcos.fractalstudio.domain.timeline.Timeline;
import com.marcos.fractalstudio.infrastructure.rendering.AdaptiveEscapeBudget;
import com.marcos.fractalstudio.presentation.common.UiThreadExecutor;
import com.marcos.fractalstudio.presentation.renderqueue.RenderJobRow;
import com.marcos.fractalstudio.presentation.timeline.KeyframeTimelineItem;

import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Presentation-facing state holder for the desktop shell.
 * It coordinates project persistence, preview requests, render jobs and derived labels.
 */
public final class StudioShellViewModel {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Duration INTERACTION_PREVIEW_DEBOUNCE = Duration.millis(220);
    private static final double VIEWPORT_RESIZE_EPSILON = 8.0;
    private static final CameraState DEFAULT_CAMERA_STATE = new CameraState(
            new FractalCoordinate(-0.5, 0.0),
            new ZoomLevel(1.0)
    );
    private static final double QUICK_ZOOM_IN_FACTOR = 1.18;
    private static final double QUICK_ZOOM_OUT_FACTOR = 0.85;
    private static final double KEYBOARD_PAN_STEP = 0.10;
    private static final MathContext CAMERA_MATH_CONTEXT = MathContext.DECIMAL128;

    private final ProjectFacade projectFacade;
    private final RenderFacade renderFacade;
    private final RenderHistoryFacade renderHistoryFacade;
    private final ExportFacade exportFacade;
    private final UiThreadExecutor uiThreadExecutor;
    private final StudioStoragePaths storagePaths;
    private final StudioPreviewCoordinator previewCoordinator;
    private final Map<String, RenderJobRow> renderJobRowsById = new LinkedHashMap<>();
    private final Map<String, BookmarkSidebarItem> bookmarkSidebarItemsById = new LinkedHashMap<>();
    private final PauseTransition previewRefreshDebounce = new PauseTransition(Duration.ZERO);
    private final AtomicLong thumbnailGeneration = new AtomicLong();
    private final AtomicLong bookmarkThumbnailGeneration = new AtomicLong();

    private final ObjectProperty<Image> previewImage = new SimpleObjectProperty<>();
    private final ObjectProperty<TreeItem<SidebarTreeNode>> projectTreeRoot =
            new SimpleObjectProperty<>(new TreeItem<>(SidebarTreeNode.root("Project")));
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
    private final StringProperty viewportStatus = new SimpleStringProperty("Esperando preview");
    private final StringProperty metricsText = new SimpleStringProperty("");
    private final SimpleObjectProperty<WorkspaceDrawerTab> workspaceDrawerTab = new SimpleObjectProperty<>(WorkspaceDrawerTab.POINTS);
    private final javafx.beans.property.SimpleBooleanProperty workspaceDrawerVisible = new javafx.beans.property.SimpleBooleanProperty(false);
    private final ObservableList<KeyframeDto> keyframes = FXCollections.observableArrayList();
    private final ObservableList<KeyframeTimelineItem> timelineItems = FXCollections.observableArrayList();
    private final ObservableList<RenderJobRow> renderJobs = FXCollections.observableArrayList();
    private final ObjectProperty<DeepZoomAdvisory> pendingDeepZoomAdvisory = new SimpleObjectProperty<>();

    private Project currentProject;
    private Path currentProjectFilePath;
    private CameraState currentCameraState;
    private double viewportWidth = 960.0;
    private double viewportHeight = 540.0;
    private int selectedBookmarkIndex = -1;

    /**
     * Creates the shared shell view model used across the desktop UI.
     *
     * @param projectFacade application boundary for project editing and persistence
     * @param renderFacade application boundary for preview and render jobs
     * @param renderHistoryFacade persistence boundary for render history
     * @param exportFacade application boundary for archive export
     * @param uiThreadExecutor JavaFX thread dispatcher
     * @param storageRoot base storage directory used by the desktop app
     */
    public StudioShellViewModel(
            ProjectFacade projectFacade,
            RenderFacade renderFacade,
            RenderHistoryFacade renderHistoryFacade,
            ExportFacade exportFacade,
            UiThreadExecutor uiThreadExecutor,
            Path storageRoot
    ) {
        this.projectFacade = projectFacade;
        this.renderFacade = renderFacade;
        this.renderHistoryFacade = renderHistoryFacade;
        this.exportFacade = exportFacade;
        this.uiThreadExecutor = uiThreadExecutor;
        this.storagePaths = StudioStoragePaths.from(storageRoot);
        this.previewCoordinator = new StudioPreviewCoordinator(
                renderFacade,
                uiThreadExecutor,
                viewportStatus::set,
                this::acceptRenderedPreview
        );
        this.previewRefreshDebounce.setOnFinished(event -> requestPreviewWhenPossible());
        purgeSessionStorage();
        createNewProject(false);
    }

    /**
     * Replaces the current project with a new empty project and persists it immediately.
     */
    public void createNewProject() {
        createNewProject(true);
    }

    private void createNewProject(boolean shouldRequestPreview) {
        currentProject = projectFacade.createProject("Fractal Render Studio");
        currentProject = synchronizePointsProject(currentProject);
        currentProjectFilePath = null;
        currentCameraState = DEFAULT_CAMERA_STATE;
        selectedBookmarkIndex = -1;
        renderJobRowsById.clear();
        renderJobs.clear();
        resetPreviewSurface();
        appendMetric("Proyecto creado: " + currentProject.name().value());
        refreshDerivedState();
        if (shouldRequestPreview) {
            requestAutoPreview();
        }
    }

    /**
     * Discards in-memory session state and removes internal runtime artifacts.
     */
    public void shutdownSession() {
        previewRefreshDebounce.stop();
        previewCoordinator.invalidate();
        pendingDeepZoomAdvisory.set(null);
        thumbnailGeneration.incrementAndGet();
        bookmarkThumbnailGeneration.incrementAndGet();
        previewImage.set(null);
        renderJobRowsById.clear();
        bookmarkSidebarItemsById.clear();
        renderJobs.clear();
        timelineItems.clear();
        keyframes.clear();
        purgeSessionStorage();
    }

    /**
     * Captures the current camera as a reusable point and keeps the sidebar and
     * animation timeline in sync by creating both the saved point and its
     * timeline counterpart with the same label.
     */
    public void addPoint() {
        String pointLabel = nextPointLabel();
        currentProject = projectFacade.addBookmark(currentProject, currentCameraState);
        ProjectBookmark bookmark = currentProject.bookmarks().getLast();
        currentProject = projectFacade.renameBookmark(currentProject, bookmark.id().value(), pointLabel);
        currentProject = projectFacade.createKeyframeFromBookmark(currentProject, bookmark.id().value());
        String keyframeId = currentProject.timeline().keyframes().getLast().id().value();
        currentProject = projectFacade.renameKeyframe(currentProject, keyframeId, pointLabel);
        selectedBookmarkIndex = currentProject.bookmarks().size() - 1;
        refreshDerivedState();
        appendMetric("Punto guardado: " + pointLabel);
    }

    /**
     * Captures the current camera state as a timeline point.
     */
    public void addKeyframe() {
        addPoint();
    }

    /**
     * Renames a keyframe in the timeline.
     *
     * @param keyframeId keyframe identifier
     * @param newLabel updated label
     */
    public void renameKeyframe(String keyframeId, String newLabel) {
        String normalizedLabel = newLabel == null ? "" : newLabel.trim();
        if (normalizedLabel.isBlank()) {
            return;
        }
        String currentLabel = currentProject.timeline().keyframes().stream()
                .filter(keyframe -> keyframe.id().value().equals(keyframeId))
                .map(com.marcos.fractalstudio.domain.timeline.Keyframe::label)
                .findFirst()
                .orElse(normalizedLabel);
        String linkedBookmarkId = findLinkedBookmarkIdForKeyframe(keyframeId);
        currentProject = projectFacade.renameKeyframe(currentProject, keyframeId, normalizedLabel);
        if (linkedBookmarkId != null) {
            currentProject = projectFacade.renameBookmark(currentProject, linkedBookmarkId, normalizedLabel);
        }
        currentProject = synchronizePointsProject(currentProject);
        refreshDerivedState();
        appendMetric("Punto renombrado: " + currentLabel + " -> " + normalizedLabel);
    }

    /**
     * Deletes a keyframe from the project timeline.
     *
     * @param keyframeId keyframe identifier
     */
    public void deleteKeyframe(String keyframeId) {
        int keyframeCountBefore = currentProject.timeline().keyframes().size();
        String linkedBookmarkId = findLinkedBookmarkIdForKeyframe(keyframeId);
        currentProject = projectFacade.deleteKeyframe(currentProject, keyframeId);
        if (currentProject.timeline().keyframes().size() == keyframeCountBefore) {
            return;
        }
        if (linkedBookmarkId != null) {
            currentProject = projectFacade.deleteBookmark(currentProject, linkedBookmarkId);
        }
        currentProject = synchronizePointsProject(currentProject);
        refreshDerivedState();
        appendMetric("Punto eliminado");
        requestAutoPreview();
    }

    public void addBookmark() {
        addPoint();
    }

    public void deleteBookmark(String bookmarkId) {
        int bookmarkCountBefore = currentProject.bookmarks().size();
        String linkedKeyframeId = findLinkedKeyframeIdForBookmark(bookmarkId);
        currentProject = projectFacade.deleteBookmark(currentProject, bookmarkId);
        if (currentProject.bookmarks().size() == bookmarkCountBefore) {
            return;
        }
        if (linkedKeyframeId != null) {
            currentProject = projectFacade.deleteKeyframe(currentProject, linkedKeyframeId);
        }
        selectedBookmarkIndex = Math.min(selectedBookmarkIndex, currentProject.bookmarks().size() - 1);
        currentProject = synchronizePointsProject(currentProject);
        refreshDerivedState();
        appendMetric("Punto eliminado");
        requestAutoPreview();
    }

    /**
     * Renames a stored mathematical bookmark without altering its camera state.
     *
     * @param bookmarkId bookmark identifier
     * @param newLabel user-facing label to persist
     */
    public void renameBookmark(String bookmarkId, String newLabel) {
        String normalizedLabel = newLabel == null ? "" : newLabel.trim();
        if (normalizedLabel.isBlank()) {
            return;
        }
        String currentLabel = currentProject.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .map(ProjectBookmark::label)
                .findFirst()
                .orElse(normalizedLabel);
        String linkedKeyframeId = findLinkedKeyframeIdForBookmark(bookmarkId);
        currentProject = projectFacade.renameBookmark(currentProject, bookmarkId, normalizedLabel);
        if (linkedKeyframeId != null) {
            currentProject = projectFacade.renameKeyframe(currentProject, linkedKeyframeId, normalizedLabel);
        }
        currentProject = synchronizePointsProject(currentProject);
        refreshDerivedState();
        appendMetric("Punto renombrado: " + currentLabel + " -> " + normalizedLabel);
    }

    /**
     * Reorders a bookmark inside the sidebar list.
     *
     * @param bookmarkId bookmark identifier
     * @param direction negative for up, positive for down
     */
    public void moveBookmark(String bookmarkId, int direction) {
        if (direction == 0) {
            return;
        }
        int previousIndex = indexOfBookmark(bookmarkId);
        if (previousIndex < 0) {
            return;
        }
        int targetIndex = Math.max(0, Math.min(currentProject.bookmarks().size() - 1, previousIndex + direction));
        if (targetIndex == previousIndex) {
            return;
        }
        currentProject = projectFacade.moveBookmark(currentProject, bookmarkId, direction);
        selectedBookmarkIndex = targetIndex;
        currentProject = synchronizePointsProject(currentProject);
        refreshDerivedState();
        appendMetric("Punto reordenado");
    }

    public void createKeyframeFromBookmark(String bookmarkId) {
        int keyframeCountBefore = currentProject.timeline().keyframes().size();
        currentProject = projectFacade.createKeyframeFromBookmark(currentProject, bookmarkId);
        if (currentProject.timeline().keyframes().size() == keyframeCountBefore) {
            return;
        }
        String bookmarkLabel = currentProject.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .map(ProjectBookmark::label)
                .findFirst()
                .orElse("Punto");
        String createdKeyframeId = currentProject.timeline().keyframes().getLast().id().value();
        currentProject = projectFacade.renameKeyframe(currentProject, createdKeyframeId, bookmarkLabel);
        currentProject = synchronizePointsProject(currentProject);
        refreshDerivedState();
        appendMetric("Punto agregado al timeline");
    }

    public void submitRender() {
        double durationSeconds = currentSuggestedRenderDurationSeconds();
        submitRender(
                currentSuggestedRenderWorkspaceName(),
                estimateRenderFrameCount(durationSeconds, currentProject.settings().defaultFramesPerSecond()),
                currentProject.settings().defaultFramesPerSecond(),
                defaultRenderWorkspaceBaseDirectory(),
                currentProject.settings().defaultRenderPreset().name()
        );
    }

    /**
     * Forces an immediate precise preview using the current project, camera and viewport state.
     */
    public void requestPreview() {
        previewRefreshDebounce.stop();
        requestPreviewWhenPossible(true, false);
    }

    /**
     * Requests an immediate adaptive preview intended for startup or navigation jumps.
     */
    public void requestAutoPreview() {
        previewRefreshDebounce.stop();
        requestPreviewWhenPossible(false, false);
    }

    /**
     * Forces an immediate precise preview and enables advisory dialogs when the
     * current deep-zoom state is expected to be expensive.
     */
    public void requestManualPreview() {
        previewRefreshDebounce.stop();
        requestPreviewWhenPossible(true, true);
    }

    /**
     * Schedules a preview after a short debounce window suitable for user interaction.
     */
    public void requestPreviewAfterInteraction() {
        previewRefreshDebounce.stop();
        previewRefreshDebounce.setDuration(INTERACTION_PREVIEW_DEBOUNCE);
        previewRefreshDebounce.playFromStart();
    }

    /**
     * Updates the effective viewport used by the renderer.
     *
     * @param width latest viewport width in pixels
     * @param height latest viewport height in pixels
     */
    public void updateViewportSize(double width, double height) {
        double normalizedWidth = Math.max(64.0, width);
        double normalizedHeight = Math.max(64.0, height);
        if (!hasMeaningfulViewportResize(normalizedWidth, normalizedHeight)) {
            return;
        }
        viewportWidth = normalizedWidth;
        viewportHeight = normalizedHeight;
    }

    /**
     * Moves the camera by interpreting pixel deltas in viewport space.
     *
     * @param deltaPixelsX horizontal drag delta
     * @param deltaPixelsY vertical drag delta
     */
    public void panCamera(double deltaPixelsX, double deltaPixelsY) {
        double planeHeight = 3.0 / currentCameraState.zoomLevel().value();
        double planeWidth = planeHeight * (viewportWidth / viewportHeight);
        double deltaX = (deltaPixelsX / viewportWidth) * planeWidth;
        double deltaY = (deltaPixelsY / viewportHeight) * planeHeight;

        currentCameraState = currentCameraState.pan(-deltaX, -deltaY);
        refreshCameraState();
    }

    /**
     * Multiplies the camera zoom by the provided factor.
     *
     * @param factor zoom multiplier
     */
    public boolean zoomCamera(double factor) {
        ZoomLimitPolicy.ZoomLimitDecision zoomDecision = ZoomLimitPolicy.evaluate(
                currentProject.fractalFormula(),
                currentCameraState.zoomLevel().valueDecimal(),
                factor
        );
        if (zoomDecision.blocked()) {
            handleZoomLimitReached();
            return false;
        }
        currentCameraState = new CameraState(currentCameraState.center(), new ZoomLevel(zoomDecision.targetZoom()));
        refreshCameraState();
        return true;
    }

    /**
     * Zooms using a viewport anchor point so the cursor position remains stable
     * in fractal space whenever the request is accepted.
     *
     * @param factor zoom multiplier
     * @param anchorPixelX viewport-space anchor x
     * @param anchorPixelY viewport-space anchor y
     * @return {@code true} when the zoom was applied, {@code false} when blocked by policy
     */
    public boolean zoomCameraAt(double factor, double anchorPixelX, double anchorPixelY) {
        ZoomLimitPolicy.ZoomLimitDecision zoomDecision = ZoomLimitPolicy.evaluate(
                currentProject.fractalFormula(),
                currentCameraState.zoomLevel().valueDecimal(),
                factor
        );
        if (zoomDecision.blocked()) {
            handleZoomLimitReached();
            return false;
        }
        double normalizedAnchorX = clamp(anchorPixelX / viewportWidth, 0.0, 1.0);
        double normalizedAnchorY = clamp(anchorPixelY / viewportHeight, 0.0, 1.0);

        BigDecimal currentZoom = currentCameraState.zoomLevel().valueDecimal();
        BigDecimal newZoom = zoomDecision.targetZoom();
        BigDecimal aspectRatio = BigDecimal.valueOf(viewportWidth / viewportHeight);

        BigDecimal currentPlaneHeight = BigDecimal.valueOf(3.0).divide(currentZoom, CAMERA_MATH_CONTEXT);
        BigDecimal currentPlaneWidth = currentPlaneHeight.multiply(aspectRatio, CAMERA_MATH_CONTEXT);
        BigDecimal newPlaneHeight = BigDecimal.valueOf(3.0).divide(newZoom, CAMERA_MATH_CONTEXT);
        BigDecimal newPlaneWidth = newPlaneHeight.multiply(aspectRatio, CAMERA_MATH_CONTEXT);

        BigDecimal currentMinX = currentCameraState.center().xDecimal().subtract(
                currentPlaneWidth.divide(BigDecimal.valueOf(2.0), CAMERA_MATH_CONTEXT),
                CAMERA_MATH_CONTEXT
        );
        BigDecimal currentMinY = currentCameraState.center().yDecimal().subtract(
                currentPlaneHeight.divide(BigDecimal.valueOf(2.0), CAMERA_MATH_CONTEXT),
                CAMERA_MATH_CONTEXT
        );

        BigDecimal anchorXRatio = BigDecimal.valueOf(normalizedAnchorX);
        BigDecimal anchorYRatio = BigDecimal.valueOf(normalizedAnchorY);
        BigDecimal anchorFractalX = currentMinX.add(currentPlaneWidth.multiply(anchorXRatio, CAMERA_MATH_CONTEXT), CAMERA_MATH_CONTEXT);
        BigDecimal anchorFractalY = currentMinY.add(currentPlaneHeight.multiply(anchorYRatio, CAMERA_MATH_CONTEXT), CAMERA_MATH_CONTEXT);

        BigDecimal newMinX = anchorFractalX.subtract(newPlaneWidth.multiply(anchorXRatio, CAMERA_MATH_CONTEXT), CAMERA_MATH_CONTEXT);
        BigDecimal newMinY = anchorFractalY.subtract(newPlaneHeight.multiply(anchorYRatio, CAMERA_MATH_CONTEXT), CAMERA_MATH_CONTEXT);
        BigDecimal newCenterX = newMinX.add(newPlaneWidth.divide(BigDecimal.valueOf(2.0), CAMERA_MATH_CONTEXT), CAMERA_MATH_CONTEXT);
        BigDecimal newCenterY = newMinY.add(newPlaneHeight.divide(BigDecimal.valueOf(2.0), CAMERA_MATH_CONTEXT), CAMERA_MATH_CONTEXT);

        currentCameraState = new CameraState(
                new FractalCoordinate(newCenterX, newCenterY),
                new ZoomLevel(newZoom)
        );
        refreshCameraState();
        return true;
    }

    public boolean zoomIn() {
        boolean zoomApplied = zoomCamera(QUICK_ZOOM_IN_FACTOR);
        if (zoomApplied) {
            requestPreviewAfterInteraction();
        }
        return zoomApplied;
    }

    public boolean zoomOut() {
        boolean zoomApplied = zoomCamera(QUICK_ZOOM_OUT_FACTOR);
        if (zoomApplied) {
            requestPreviewAfterInteraction();
        }
        return zoomApplied;
    }

    public void resetCamera() {
        currentCameraState = DEFAULT_CAMERA_STATE;
        refreshCameraState();
        appendMetric("Camara restablecida al encuadre inicial");
        requestAutoPreview();
    }

    public void nudgeCameraLeft() {
        nudgeCamera(-KEYBOARD_PAN_STEP, 0.0);
    }

    public void nudgeCameraRight() {
        nudgeCamera(KEYBOARD_PAN_STEP, 0.0);
    }

    public void nudgeCameraUp() {
        nudgeCamera(0.0, KEYBOARD_PAN_STEP);
    }

    public void nudgeCameraDown() {
        nudgeCamera(0.0, -KEYBOARD_PAN_STEP);
    }

    public void jumpToPreviousBookmark() {
        jumpToBookmark(-1);
    }

    public void jumpToNextBookmark() {
        jumpToBookmark(1);
    }

    /**
     * Persists the current project snapshot to the autosave location.
     */
    public void saveProject() {
        if (currentProjectFilePath == null) {
            appendMetric("Usa \"Guardar proyecto...\" para exportar el proyecto a un archivo");
            return;
        }
        saveProjectTo(currentProjectFilePath);
    }

    public void saveProjectTo(Path projectPath) {
        try {
            Files.createDirectories(projectPath.toAbsolutePath().getParent());
            projectFacade.saveProject(currentProject, projectPath);
            currentProjectFilePath = projectPath;
            appendMetric("Proyecto guardado en " + projectPath);
        } catch (IOException exception) {
            appendMetric("No se pudo guardar el proyecto: " + exception.getMessage());
        }
    }

    /**
     * Loads the autosaved project or creates a default project when none exists.
     */
    public void loadLastProjectOrCreateNew() {
        createNewProject(true);
    }

    private void loadLastProjectOrCreateNew(boolean shouldRequestPreview) {
        createNewProject(shouldRequestPreview);
    }

    private void loadLastProjectOrCreateNew(boolean shouldRequestPreview, boolean startupFraming) {
        createNewProject(shouldRequestPreview);
    }

    public void loadProjectFrom(Path projectPath, boolean shouldRequestPreview) {
        try {
            currentProject = synchronizePointsProject(projectFacade.loadProject(projectPath));
            currentProjectFilePath = projectPath;
            currentCameraState = resolveInitialCameraState(currentProject, false);
            selectedBookmarkIndex = -1;
            resetPreviewSurface();
            appendMetric("Proyecto cargado desde " + projectPath);
            refreshDerivedState();
            refreshRenderJobs();
            if (shouldRequestPreview) {
                requestAutoPreview();
            }
        } catch (IOException exception) {
            appendMetric("No se pudo cargar el proyecto: " + exception.getMessage());
            createNewProject(shouldRequestPreview);
        }
    }

    /**
     * Requests cooperative cancellation for a background render job.
     *
     * @param renderJobRow selected job row
     */
    public void cancelRenderJob(RenderJobRow renderJobRow) {
        if (renderJobRow == null || !renderJobRow.isCancellable()) {
            return;
        }
        boolean cancelled = renderFacade.cancelRenderJob(renderJobRow.jobId(), this::acceptRenderJobUpdate);
        appendMetric(cancelled
                ? "Cancelacion solicitada para job " + renderJobRow.jobNameProperty().get()
                : "No se pudo cancelar job " + renderJobRow.jobNameProperty().get());
        refreshRenderJobs();
    }

    /**
     * Reloads render job state from the application layer.
     */
    public void refreshRenderJobs() {
        uiThreadExecutor.execute(() -> {
            for (RenderJobStatusDto statusDto : renderFacade.listRenderJobs()) {
                RenderJobRow renderJobRow = renderJobRowsById.computeIfAbsent(statusDto.jobId(), ignored -> {
                    RenderJobRow created = new RenderJobRow(statusDto);
                    renderJobs.add(created);
                    return created;
                });
                renderJobRow.update(statusDto);
            }
            renderJobs.sort((left, right) -> right.jobId().compareTo(left.jobId()));
            persistRenderHistory();
        });
    }

    public void applyProjectSettings(
            String newProjectName,
            String description,
            double defaultFramesPerSecond,
            double defaultDurationSeconds,
            double keyframeStepSeconds,
            String defaultRenderPreset
    ) {
        currentProject = projectFacade.renameProject(currentProject, newProjectName);
        currentProject = projectFacade.updateProjectDescription(currentProject, description);
        currentProject = projectFacade.updateProjectSettings(
                currentProject,
                defaultFramesPerSecond,
                estimateRenderFrameCount(defaultDurationSeconds, defaultFramesPerSecond),
                keyframeStepSeconds,
                RenderPreset.valueOf(defaultRenderPreset)
        );
        currentProject = synchronizePointsProject(currentProject);
        refreshDerivedState();
        appendMetric("Project settings actualizados");
    }

    public List<String> availableFractalFormulas() {
        return java.util.Arrays.stream(FractalFormulaType.values())
                .map(type -> FractalFormulaFactory.create(type).name())
                .toList();
    }

    public List<String> availableColorProfiles() {
        return ColorProfileFactory.availableProfileNames();
    }

    public String currentFractalFormulaName() {
        return currentProject.fractalFormula().name();
    }

    public String currentColorProfileName() {
        return currentProject.colorProfile().name();
    }

    public int currentMaxIterations() {
        return currentProject.renderProfile().escapeParameters().maxIterations();
    }

    public double currentEscapeRadius() {
        return currentProject.renderProfile().escapeParameters().escapeRadius();
    }

    public void applyInspectorSettings(
            String fractalFormulaName,
            String colorProfileName,
            int maxIterations,
            double escapeRadius
    ) {
        FractalFormulaType fractalFormulaType = java.util.Arrays.stream(FractalFormulaType.values())
                .filter(type -> FractalFormulaFactory.create(type).name().equals(fractalFormulaName))
                .findFirst()
                .orElse(FractalFormulaType.MANDELBROT);
        currentProject = projectFacade.updateInspector(
                currentProject,
                fractalFormulaType,
                colorProfileName,
                Math.max(32, Math.min(255, maxIterations)),
                Math.max(2.0, escapeRadius)
        );
        currentProject = synchronizePointsProject(currentProject);
        refreshDerivedState();
        appendMetric("Inspector aplicado: " + fractalFormulaName + " | " + colorProfileName
                + " | " + currentProject.renderProfile().escapeParameters().maxIterations() + " iteraciones");
        requestAutoPreview();
    }

    public void submitRender(String renderName, int totalFrames, double framesPerSecond, Path baseDirectory, String renderPreset) {
        currentProject.validateRenderability();
        openRenderQueue();
        String effectiveRenderName = resolveRenderJobName(renderName);
        Path outputDirectory = buildRenderOutputDirectory(baseDirectory, effectiveRenderName);
        double durationSeconds = totalFrames / framesPerSecond;
        try {
            prepareRenderWorkspace(outputDirectory, effectiveRenderName, totalFrames, framesPerSecond, renderPreset, durationSeconds);
        } catch (IOException exception) {
            appendMetric("No se pudo preparar la carpeta de render: " + exception.getMessage());
            return;
        }
        RenderRequest renderRequest = new RenderRequest(
                currentProject,
                currentCameraState,
                effectiveRenderName,
                totalFrames,
                framesPerSecond,
                outputDirectory,
                RenderPreset.valueOf(renderPreset)
        );
        renderFacade.submitRender(renderRequest, this::acceptRenderJobUpdate);
        appendMetric("Render solicitado para " + totalFrames + " frames en " + outputDirectory + " con preset " + renderPreset);
        refreshRenderJobs();
    }

    public javafx.beans.property.BooleanProperty workspaceDrawerVisibleProperty() {
        return workspaceDrawerVisible;
    }

    public ReadOnlyObjectProperty<WorkspaceDrawerTab> workspaceDrawerTabProperty() {
        return workspaceDrawerTab;
    }

    public void togglePointsDrawer() {
        if (workspaceDrawerVisible.get() && workspaceDrawerTab.get() == WorkspaceDrawerTab.POINTS) {
            workspaceDrawerVisible.set(false);
            return;
        }
        workspaceDrawerTab.set(WorkspaceDrawerTab.POINTS);
        workspaceDrawerVisible.set(true);
    }

    public void openRenderQueue() {
        workspaceDrawerTab.set(WorkspaceDrawerTab.RENDER_QUEUE);
        workspaceDrawerVisible.set(true);
    }

    public void closeWorkspaceDrawer() {
        workspaceDrawerVisible.set(false);
    }

    public String currentProjectName() {
        return currentProject.name().value();
    }

    public String currentProjectDescription() {
        return currentProject.metadata().description();
    }

    public double currentDefaultFramesPerSecond() {
        return currentProject.settings().defaultFramesPerSecond();
    }

    public int currentDefaultRenderFrameCount() {
        return currentProject.settings().defaultRenderFrameCount();
    }

    public double currentSuggestedRenderDurationSeconds() {
        double timelineDuration = currentProject.timeline().keyframes().isEmpty()
                ? 0.0
                : currentProject.timeline().keyframes().getLast().timePosition().seconds();
        double defaultDuration = currentProject.settings().defaultRenderFrameCount()
                / currentProject.settings().defaultFramesPerSecond();
        return Math.max(1.0, Math.max(timelineDuration, defaultDuration));
    }

    public double currentKeyframeStepSeconds() {
        return currentProject.settings().keyframeStepSeconds();
    }

    public String currentDefaultRenderPreset() {
        return currentProject.settings().defaultRenderPreset().name();
    }

    public Path defaultRenderWorkspaceBaseDirectory() {
        Path desktopDirectory = Path.of(System.getProperty("user.home"), "Desktop");
        if (Files.isDirectory(desktopDirectory)) {
            return desktopDirectory;
        }
        return Path.of(System.getProperty("user.home"));
    }

    public String currentSuggestedRenderWorkspaceName() {
        return currentProject.name().value() + " Render";
    }

    public Path suggestedArchivePath(RenderJobRow renderJobRow) {
        String safeName = renderJobRow.jobName().replaceAll("[^a-zA-Z0-9-_]+", "_");
        return storagePaths.exportStorageDirectory().resolve(safeName + ".zip");
    }

    public Path suggestedVideoPath(RenderJobRow renderJobRow) {
        String safeName = renderJobRow.jobName().replaceAll("[^a-zA-Z0-9-_]+", "_");
        return storagePaths.exportStorageDirectory().resolve(safeName + ".mp4");
    }

    public void exportRenderJob(RenderJobRow renderJobRow, Path archivePath) {
        if (renderJobRow == null) {
            return;
        }
        if (!Objects.equals(renderJobRow.state(), "COMPLETED")) {
            appendMetric("Solo se pueden exportar jobs completados");
            return;
        }
        try {
            Files.createDirectories(storagePaths.exportStorageDirectory());
            ExportResult exportResult = exportFacade.exportFrames(new ExportRequest(
                    resolveFramesDirectory(Path.of(renderJobRow.outputDirectory())),
                    archivePath
            ));
            appendMetric("Exportacion completada: " + exportResult.archivePath() + " (" + exportResult.exportedFileCount() + " archivos)");
        } catch (Exception exception) {
            appendMetric("Exportacion fallida: " + exception.getMessage());
        }
    }

    public void exportRenderVideo(RenderJobRow renderJobRow, Path destinationVideo) {
        if (renderJobRow == null) {
            return;
        }
        if (!Objects.equals(renderJobRow.state(), "COMPLETED")) {
            appendMetric("Solo se pueden exportar videos de jobs completados");
            return;
        }
        try {
            Files.createDirectories(storagePaths.exportStorageDirectory());
            Path sourceVideo = Path.of(renderJobRow.outputDirectory()).resolve("render.mp4");
            if (!Files.exists(sourceVideo)) {
                appendMetric("No se encontro el video MP4 generado");
                return;
            }
            Files.copy(sourceVideo, destinationVideo, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            appendMetric("Video exportado: " + destinationVideo);
        } catch (Exception exception) {
            appendMetric("Exportacion de video fallida: " + exception.getMessage());
        }
    }

    public ReadOnlyObjectProperty<Image> previewImageProperty() {
        return previewImage;
    }

    public ObjectProperty<Image> bookmarkThumbnailProperty(String bookmarkId) {
        BookmarkSidebarItem item = bookmarkSidebarItemsById.get(bookmarkId);
        if (item == null) {
            return new SimpleObjectProperty<>();
        }
        return item.thumbnailProperty();
    }

    /**
     * Returns the persisted label of a bookmark so dialogs can avoid using
     * condensed sidebar text.
     *
     * @param bookmarkId bookmark identifier
     * @return bookmark label or an empty string when not found
     */
    public String currentBookmarkLabel(String bookmarkId) {
        return currentProject.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .map(ProjectBookmark::label)
                .findFirst()
                .orElse("");
    }

    /**
     * Returns the persisted label of a keyframe.
     *
     * @param keyframeId keyframe identifier
     * @return keyframe label or an empty string when not found
     */
    public String currentKeyframeLabel(String keyframeId) {
        return currentProject.timeline().keyframes().stream()
                .filter(keyframe -> keyframe.id().value().equals(keyframeId))
                .map(com.marcos.fractalstudio.domain.timeline.Keyframe::label)
                .findFirst()
                .orElse("");
    }

    public boolean hasPreviewImage() {
        return previewImage.get() != null;
    }

    public ObservableList<KeyframeDto> keyframes() {
        return keyframes;
    }

    public ObservableList<KeyframeTimelineItem> timelineItems() {
        return timelineItems;
    }

    public ObservableList<RenderJobRow> renderJobs() {
        return renderJobs;
    }

    public ReadOnlyObjectProperty<TreeItem<SidebarTreeNode>> projectTreeRootProperty() {
        return projectTreeRoot;
    }

    public ReadOnlyStringProperty projectNameProperty() {
        return projectName;
    }

    public ReadOnlyStringProperty formulaNameProperty() {
        return formulaName;
    }

    public ReadOnlyStringProperty colorProfileNameProperty() {
        return colorProfileName;
    }

    public ReadOnlyStringProperty renderProfileNameProperty() {
        return renderProfileName;
    }

    public ReadOnlyStringProperty resolutionLabelProperty() {
        return resolutionLabel;
    }

    public ReadOnlyStringProperty cameraCenterLabelProperty() {
        return cameraCenterLabel;
    }

    public ReadOnlyStringProperty cameraZoomLabelProperty() {
        return cameraZoomLabel;
    }

    public ReadOnlyStringProperty previewIterationsLabelProperty() {
        return previewIterationsLabel;
    }

    public ReadOnlyStringProperty previewModeLabelProperty() {
        return previewModeLabel;
    }

    public ReadOnlyStringProperty deepZoomHealthLabelProperty() {
        return deepZoomHealthLabel;
    }

    public ReadOnlyStringProperty deepZoomMemoryLabelProperty() {
        return deepZoomMemoryLabel;
    }

    public ReadOnlyIntegerProperty configuredMaxIterationsProperty() {
        return configuredMaxIterations;
    }

    public ReadOnlyDoubleProperty configuredEscapeRadiusProperty() {
        return configuredEscapeRadius;
    }

    public ReadOnlyStringProperty defaultRenderPresetLabelProperty() {
        return defaultRenderPresetLabel;
    }

    public ReadOnlyStringProperty bookmarkSummaryProperty() {
        return bookmarkSummary;
    }

    public ReadOnlyStringProperty timelineSummaryProperty() {
        return timelineSummary;
    }

    public ReadOnlyStringProperty projectDescriptionProperty() {
        return projectDescription;
    }

    public ReadOnlyStringProperty projectCreatedAtProperty() {
        return projectCreatedAt;
    }

    public ReadOnlyStringProperty projectUpdatedAtProperty() {
        return projectUpdatedAt;
    }

    public ReadOnlyStringProperty projectDefaultFpsProperty() {
        return projectDefaultFps;
    }

    public ReadOnlyStringProperty viewportStatusProperty() {
        return viewportStatus;
    }

    public ReadOnlyStringProperty metricsTextProperty() {
        return metricsText;
    }

    public ReadOnlyObjectProperty<DeepZoomAdvisory> pendingDeepZoomAdvisoryProperty() {
        return pendingDeepZoomAdvisory;
    }

    public void clearPendingDeepZoomAdvisory() {
        pendingDeepZoomAdvisory.set(null);
    }

    private void acceptRenderJobUpdate(RenderJobStatusDto statusDto) {
        uiThreadExecutor.execute(() -> {
            RenderJobRow renderJobRow = renderJobRowsById.computeIfAbsent(statusDto.jobId(), ignored -> {
                RenderJobRow created = new RenderJobRow(statusDto);
                renderJobs.add(0, created);
                return created;
            });
            renderJobRow.update(statusDto);
            renderJobs.sort((left, right) -> right.jobId().compareTo(left.jobId()));
            persistRenderHistory();
            appendMetric(statusDto.jobName() + " -> " + statusDto.state().name() + " (" + statusDto.completedFrames()
                    + "/" + statusDto.totalFrames() + ")");
        });
    }

    private void refreshDerivedState() {
        currentProject = synchronizePointsProject(currentProject);
        applyProjectPresentation(StudioProjectPresentation.from(currentProject, currentCameraState, projectFacade));
    }

    private void applyProjectPresentation(StudioProjectPresentation presentation) {
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
        deepZoomMemoryLabel.set(captureMemoryPressure().compactLabel());
        defaultRenderPresetLabel.set(presentation.defaultRenderPresetLabel());
        bookmarkSummary.set(presentation.bookmarkSummary());
        configuredMaxIterations.set(currentProject.renderProfile().escapeParameters().maxIterations());
        configuredEscapeRadius.set(currentProject.renderProfile().escapeParameters().escapeRadius());
        timelineSummary.set(presentation.timelineSummary());
        projectDescription.set(presentation.projectDescription());
        projectCreatedAt.set(presentation.projectCreatedAt());
        projectUpdatedAt.set(presentation.projectUpdatedAt());
        projectDefaultFps.set(presentation.projectDefaultFps());
        keyframes.setAll(presentation.keyframes());
        timelineItems.setAll(buildTimelineItems(presentation.keyframes()));
        projectTreeRoot.set(presentation.projectTreeRoot());
        rebuildBookmarkSidebarItems();
        regenerateTimelineThumbnails();
        regenerateBookmarkThumbnails();
    }

    private void refreshCameraState() {
        cameraCenterLabel.set(formatDouble(currentCameraState.center().x()) + ", " + formatDouble(currentCameraState.center().y()));
        cameraZoomLabel.set(formatDouble(currentCameraState.zoomLevel().value()) + "x");
        previewIterationsLabel.set(AdaptiveEscapeBudget.previewIterations(
                currentProject.renderProfile().escapeParameters().maxIterations(),
                currentCameraState.zoomLevel().value()
        ) + " iteraciones");
    }

    private WritableImage convertToImage(RenderedFrame renderedFrame) {
        WritableImage writableImage = new WritableImage(renderedFrame.width(), renderedFrame.height());
        writableImage.getPixelWriter().setPixels(
                0,
                0,
                renderedFrame.width(),
                renderedFrame.height(),
                PixelFormat.getIntArgbInstance(),
                renderedFrame.argbPixels(),
                0,
                renderedFrame.width()
        );
        return writableImage;
    }

    private void appendMetric(String message) {
        String line = "[" + LocalTime.now().format(TIME_FORMATTER) + "] " + message;
        String currentText = metricsText.get();
        metricsText.set(currentText.isBlank() ? line : line + System.lineSeparator() + currentText);
    }

    private Path resolveFramesDirectory(Path outputDirectory) {
        Path framesDirectory = outputDirectory.resolve("frames");
        return Files.exists(framesDirectory) ? framesDirectory : outputDirectory;
    }

    private void acceptRenderedPreview(RenderedFrame renderedFrame) {
        previewImage.set(convertToImage(renderedFrame));
    }

    private void persistRenderHistory() {
        // The desktop session is intentionally ephemeral; previous render jobs are not restored.
    }

    private Path buildRenderOutputDirectory(Path baseDirectory, String renderName) {
        Path normalizedBaseDirectory = (baseDirectory == null ? defaultRenderWorkspaceBaseDirectory() : baseDirectory).toAbsolutePath();
        String safeName = normalizeRenderWorkspaceName(renderName);
        Path candidate = normalizedBaseDirectory.resolve(safeName);
        if (Files.notExists(candidate)) {
            return candidate;
        }
        return normalizedBaseDirectory.resolve(safeName + "-" + System.currentTimeMillis());
    }

    private String normalizeRenderWorkspaceName(String renderName) {
        String fallbackName = currentProject == null ? "fractal-render" : currentProject.name().value() + "-render";
        String rawName = renderName == null || renderName.isBlank() ? fallbackName : renderName.trim();
        String normalized = rawName.replaceAll("[^a-zA-Z0-9-_]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("(^-+)|(-+$)", "");
        return normalized.isBlank() ? "fractal-render" : normalized;
    }

    private String resolveRenderJobName(String renderName) {
        String fallbackName = currentProject == null ? "Fractal Render" : currentProject.name().value() + " Render";
        return renderName == null || renderName.isBlank() ? fallbackName : renderName.trim();
    }

    private void purgeSessionStorage() {
        deleteRecursively(storagePaths.projectStorageDirectory());
        deleteRecursively(storagePaths.renderStorageDirectory());
        deleteRecursively(storagePaths.exportStorageDirectory());
    }

    private void prepareRenderWorkspace(
            Path outputDirectory,
            String renderName,
            int totalFrames,
            double framesPerSecond,
            String renderPreset,
            double durationSeconds
    ) throws IOException {
        Files.createDirectories(outputDirectory);
        Path projectSnapshotPath = outputDirectory.resolve("project.fractalstudio.json");
        projectFacade.saveProject(currentProject, projectSnapshotPath);
        appendMetric("Proyecto del render guardado en " + projectSnapshotPath + " | " + totalFrames + " frames a "
                + framesPerSecond + " fps | " + durationSeconds + " s | " + renderPreset + " | " + renderName);
    }

    private void deleteRecursively(Path path) {
        if (path == null || Files.notExists(path)) {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(path)) {
            stream.sorted(java.util.Comparator.reverseOrder()).forEach(candidate -> {
                try {
                    Files.deleteIfExists(candidate);
                } catch (IOException ignored) {
                    // Best effort cleanup for ephemeral session artifacts.
                }
            });
        } catch (IOException ignored) {
            // Best effort cleanup for ephemeral session artifacts.
        }
    }

    private boolean hasMeaningfulViewportResize(double normalizedWidth, double normalizedHeight) {
        return Math.abs(viewportWidth - normalizedWidth) >= VIEWPORT_RESIZE_EPSILON
                || Math.abs(viewportHeight - normalizedHeight) >= VIEWPORT_RESIZE_EPSILON;
    }

    private void requestPreviewWhenPossible() {
        requestPreviewWhenPossible(false, false);
    }

    private void requestPreviewWhenPossible(boolean precise, boolean manual) {
        AdaptivePreviewQualityPolicy.PreviewRequestPlan previewPlan = AdaptivePreviewQualityPolicy.plan(
                viewportWidth,
                viewportHeight,
                currentCameraState.zoomLevel().value(),
                currentProject.renderProfile().escapeParameters().maxIterations(),
                precise
        );
        previewModeLabel.set(previewPlan.statusLabel());
        previewIterationsLabel.set(previewPlan.maxIterations() + " iteraciones");
        DeepZoomAdvisory deepZoomAdvisory = DeepZoomAdvisor.evaluate(
                currentCameraState.zoomLevel().value(),
                previewPlan.maxIterations(),
                viewportWidth,
                viewportHeight,
                precise,
                previewPlan.highPrecisionEnabled(),
                captureMemoryPressure()
        );
        deepZoomHealthLabel.set(deepZoomAdvisory.healthLabel());
        deepZoomMemoryLabel.set(deepZoomAdvisory.memoryLabel());
        if (manual && deepZoomAdvisory.showDialog()) {
            pendingDeepZoomAdvisory.set(deepZoomAdvisory);
        }
        previewCoordinator.request(currentProject, currentCameraState, previewPlan);
    }

    private String formatDouble(double value) {
        return String.format("%.4f", value);
    }

    private void rebuildBookmarkSidebarItems() {
        bookmarkSidebarItemsById.clear();
        for (ProjectBookmark bookmark : currentProject.bookmarks()) {
            bookmarkSidebarItemsById.put(
                    bookmark.id().value(),
                    new BookmarkSidebarItem(bookmark.id().value(), bookmark.cameraState())
            );
        }
    }

    private int indexOfBookmark(String bookmarkId) {
        for (int index = 0; index < currentProject.bookmarks().size(); index++) {
            if (currentProject.bookmarks().get(index).id().value().equals(bookmarkId)) {
                return index;
            }
        }
        return -1;
    }

    private CameraState resolveInitialCameraState(Project project, boolean startupFraming) {
        if (startupFraming) {
            return defaultCameraFor(project);
        }
        if (project.timeline().keyframes().isEmpty()) {
            return defaultCameraFor(project);
        }
        return project.timeline().keyframes().getLast().cameraState();
    }

    private CameraState defaultCameraFor(Project project) {
        String formulaName = project.fractalFormula().name();
        if ("Burning Ship".equals(formulaName)) {
            return new CameraState(
                    new FractalCoordinate(-0.45, -0.58),
                    new ZoomLevel(1.35)
            );
        }
        if ("Tricorn".equals(formulaName)) {
            return new CameraState(
                    new FractalCoordinate(0.0, 0.0),
                    new ZoomLevel(1.15)
            );
        }
        if ("Celtic Mandelbrot".equals(formulaName)) {
            return new CameraState(
                    new FractalCoordinate(-0.45, 0.0),
                    new ZoomLevel(1.05)
            );
        }
        return DEFAULT_CAMERA_STATE;
    }

    private void resetPreviewSurface() {
        previewCoordinator.invalidate();
        previewImage.set(null);
        previewModeLabel.set("Preview preciso 100%");
        deepZoomHealthLabel.set("Estable");
        deepZoomMemoryLabel.set(captureMemoryPressure().compactLabel());
        viewportStatus.set("Listo para renderizar");
    }

    private void nudgeCamera(double horizontalStep, double verticalStep) {
        double planeHeight = 3.0 / currentCameraState.zoomLevel().value();
        double planeWidth = planeHeight * (viewportWidth / viewportHeight);
        currentCameraState = currentCameraState.pan(planeWidth * horizontalStep, planeHeight * verticalStep);
        refreshCameraState();
        requestPreviewAfterInteraction();
    }

    private void jumpToBookmark(int direction) {
        if (currentProject.bookmarks().isEmpty()) {
            appendMetric("No hay puntos guardados");
            return;
        }
        if (selectedBookmarkIndex < 0) {
            selectedBookmarkIndex = direction > 0 ? 0 : currentProject.bookmarks().size() - 1;
        } else {
            selectedBookmarkIndex = Math.floorMod(selectedBookmarkIndex + direction, currentProject.bookmarks().size());
        }
        ProjectBookmark bookmark = currentProject.bookmarks().get(selectedBookmarkIndex);
        currentCameraState = bookmark.cameraState();
        refreshCameraState();
        appendMetric("Punto cargado: " + bookmark.label());
        requestAutoPreview();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public int estimateRenderFrameCount(double durationSeconds, double framesPerSecond) {
        return Math.max(1, (int) Math.ceil(durationSeconds * framesPerSecond) + 1);
    }

    private MemoryPressureSnapshot captureMemoryPressure() {
        Runtime runtime = Runtime.getRuntime();
        long committedBytes = runtime.totalMemory();
        long usedBytes = committedBytes - runtime.freeMemory();
        return new MemoryPressureSnapshot(usedBytes, committedBytes, runtime.maxMemory());
    }

    private void handleZoomLimitReached() {
        viewportStatus.set("Limite de zoom alcanzado para " + currentProject.fractalFormula().name());
        appendMetric("Limite de zoom alcanzado (" + ZoomLimitPolicy.maxZoomFor(currentProject.fractalFormula()) + ")");
    }

    private RenderJobStatusDto toRenderJobStatusDto(RenderJobRow row) {
        return new RenderJobStatusDto(
                row.jobId(),
                row.jobName(),
                com.marcos.fractalstudio.application.dto.RenderJobState.valueOf(row.state()),
                row.completedFrames(),
                row.totalFrames(),
                row.progress(),
                row.message(),
                row.outputDirectory()
        );
    }

    public void focusKeyframe(String keyframeId) {
        currentProject.timeline().keyframes().stream()
                .filter(keyframe -> keyframe.id().value().equals(keyframeId))
                .findFirst()
                .ifPresent(keyframe -> {
                    currentCameraState = keyframe.cameraState();
                    refreshCameraState();
                    appendMetric("Keyframe cargado: " + keyframe.label());
                    requestAutoPreview();
                });
    }

    /**
     * Loads a saved bookmark camera into the viewport and refreshes the preview.
     *
     * @param bookmarkId bookmark identifier from the sidebar or navigation controls
     */
    public void focusBookmark(String bookmarkId) {
        currentProject.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .findFirst()
                .ifPresent(bookmark -> {
                    selectedBookmarkIndex = currentProject.bookmarks().indexOf(bookmark);
                    currentCameraState = bookmark.cameraState();
                    refreshCameraState();
                    appendMetric("Punto cargado: " + bookmark.label());
                    requestAutoPreview();
                });
    }

    private String nextPointLabel() {
        int nextIndex = Math.max(currentProject.bookmarks().size(), currentProject.timeline().keyframes().size()) + 1;
        return "P-" + nextIndex;
    }

    private String findLinkedBookmarkIdForKeyframe(String keyframeId) {
        return currentProject.timeline().keyframes().stream()
                .filter(keyframe -> keyframe.id().value().equals(keyframeId))
                .findFirst()
                .flatMap(keyframe -> currentProject.bookmarks().stream()
                        .filter(bookmark -> bookmark.label().equals(keyframe.label()))
                        .filter(bookmark -> sameCameraState(bookmark.cameraState(), keyframe.cameraState()))
                        .map(bookmark -> bookmark.id().value())
                        .findFirst())
                .orElse(null);
    }

    private String findLinkedKeyframeIdForBookmark(String bookmarkId) {
        return currentProject.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .findFirst()
                .flatMap(bookmark -> currentProject.timeline().keyframes().stream()
                        .filter(keyframe -> keyframe.label().equals(bookmark.label()))
                        .filter(keyframe -> sameCameraState(bookmark.cameraState(), keyframe.cameraState()))
                        .map(keyframe -> keyframe.id().value())
                        .findFirst())
                .orElse(null);
    }

    private boolean sameCameraState(CameraState left, CameraState right) {
        return left.center().xDecimal().compareTo(right.center().xDecimal()) == 0
                && left.center().yDecimal().compareTo(right.center().yDecimal()) == 0
                && left.zoomLevel().valueDecimal().compareTo(right.zoomLevel().valueDecimal()) == 0;
    }

    private List<KeyframeTimelineItem> buildTimelineItems(List<KeyframeDto> keyframeDtos) {
        List<com.marcos.fractalstudio.domain.timeline.Keyframe> domainKeyframes = currentProject.timeline().keyframes();
        java.util.ArrayList<KeyframeTimelineItem> items = new java.util.ArrayList<>(keyframeDtos.size());
        for (int index = 0; index < keyframeDtos.size() && index < domainKeyframes.size(); index++) {
            items.add(new KeyframeTimelineItem(keyframeDtos.get(index), domainKeyframes.get(index).cameraState()));
        }
        return items;
    }

    private void regenerateTimelineThumbnails() {
        long generation = thumbnailGeneration.incrementAndGet();
        if (timelineItems.isEmpty()) {
            return;
        }
        for (KeyframeTimelineItem timelineItem : timelineItems) {
            timelineItem.setThumbnail(null);
            timelineItem.setThumbnailStatus("Generando miniatura...");
            renderFacade.generateKeyframeThumbnail(currentProject, timelineItem.cameraState())
                    .thenAccept(renderedFrame -> uiThreadExecutor.execute(() -> {
                        if (generation != thumbnailGeneration.get()) {
                            return;
                        }
                        timelineItem.setThumbnail(convertToImage(renderedFrame));
                    }))
                    .exceptionally(throwable -> {
                        uiThreadExecutor.execute(() -> {
                            if (generation != thumbnailGeneration.get()) {
                                return;
                            }
                            timelineItem.setThumbnailStatus("Miniatura no disponible");
                        });
                        return null;
                    });
        }
    }

    private void regenerateBookmarkThumbnails() {
        long generation = bookmarkThumbnailGeneration.incrementAndGet();
        if (bookmarkSidebarItemsById.isEmpty()) {
            return;
        }
        for (BookmarkSidebarItem bookmarkItem : bookmarkSidebarItemsById.values()) {
            bookmarkItem.setThumbnail(null);
            renderFacade.generateCameraThumbnail(currentProject, bookmarkItem.cameraState())
                    .thenAccept(renderedFrame -> uiThreadExecutor.execute(() -> {
                        if (generation != bookmarkThumbnailGeneration.get()) {
                            return;
                        }
                        bookmarkItem.setThumbnail(convertToImage(renderedFrame));
                    }))
                    .exceptionally(throwable -> null);
        }
    }

    private Project synchronizePointsProject(Project project) {
        if (!project.bookmarks().isEmpty()) {
            return project.withTimeline(rebuildTimelineFromBookmarks(project.bookmarks(), project.settings().keyframeStepSeconds()));
        }
        if (!project.timeline().keyframes().isEmpty()) {
            return project.withBookmarks(rebuildBookmarksFromTimeline(project.timeline().keyframes()));
        }
        return project;
    }

    private Timeline rebuildTimelineFromBookmarks(List<ProjectBookmark> bookmarks, double keyframeStepSeconds) {
        java.util.ArrayList<Keyframe> keyframeList = new java.util.ArrayList<>(bookmarks.size());
        for (int index = 0; index < bookmarks.size(); index++) {
            ProjectBookmark bookmark = bookmarks.get(index);
            keyframeList.add(new Keyframe(
                    KeyframeId.create(),
                    new TimePosition(index * keyframeStepSeconds),
                    bookmark.cameraState(),
                    bookmark.label()
            ));
        }
        return new Timeline(keyframeList);
    }

    private List<ProjectBookmark> rebuildBookmarksFromTimeline(List<Keyframe> keyframeList) {
        java.util.ArrayList<ProjectBookmark> bookmarks = new java.util.ArrayList<>(keyframeList.size());
        for (Keyframe keyframe : keyframeList) {
            bookmarks.add(new ProjectBookmark(
                    ProjectBookmarkId.create(),
                    keyframe.label(),
                    keyframe.cameraState()
            ));
        }
        return bookmarks;
    }

    public enum WorkspaceDrawerTab {
        POINTS,
        RENDER_QUEUE
    }
}
