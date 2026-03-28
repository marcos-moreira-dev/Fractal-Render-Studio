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
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;
import com.marcos.fractalstudio.domain.color.ColorProfileFactory;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaFactory;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaType;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;
import com.marcos.fractalstudio.domain.render.RenderPreset;
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
 *
 * <p>This view model is the main bridge between the JavaFX client and the
 * application layer. It coordinates:
 *
 * <ul>
 *   <li>the current project aggregate and camera state</li>
 *   <li>preview requests, refinement and deep-zoom advisories</li>
 *   <li>render submission and render queue projection</li>
 *   <li>derived labels shown by the inspector and sidebar</li>
 *   <li>the synchronization rule that keeps saved points and timeline entries aligned</li>
 *   <li>ephemeral session cleanup so internal state does not leak across launches</li>
 * </ul>
 *
 * <p>Although it is presentation-oriented, the class deliberately keeps the UI
 * free from persistence, rendering and export details by delegating heavy work
 * to facades and specialized collaborators.
 */
public final class StudioShellViewModel {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Duration INTERACTION_PREVIEW_DEBOUNCE = Duration.millis(220);
    private static final double VIEWPORT_RESIZE_EPSILON = 8.0;
    private static final double QUICK_ZOOM_IN_FACTOR = 1.18;
    private static final double QUICK_ZOOM_OUT_FACTOR = 0.85;
    private static final double KEYBOARD_PAN_STEP = 0.10;
    private static final MathContext CAMERA_MATH_CONTEXT = MathContext.DECIMAL128;

    private final ProjectFacade projectFacade;
    private final RenderFacade renderFacade;
    private final ExportFacade exportFacade;
    private final UiThreadExecutor uiThreadExecutor;
    private final StudioStoragePaths storagePaths;
    private final StudioPreviewCoordinator previewCoordinator;
    private final StudioPointsTimelineCoordinator pointsTimelineCoordinator = new StudioPointsTimelineCoordinator();
    private final StudioRenderQueueState renderQueueState = new StudioRenderQueueState();
    private final StudioInspectorState inspectorState = new StudioInspectorState();
    private final StudioProjectSessionState projectSessionState = new StudioProjectSessionState();
    private final Map<String, BookmarkSidebarItem> bookmarkSidebarItemsById = new LinkedHashMap<>();
    private final PauseTransition previewRefreshDebounce = new PauseTransition(Duration.ZERO);
    private final AtomicLong thumbnailGeneration = new AtomicLong();
    private final AtomicLong bookmarkThumbnailGeneration = new AtomicLong();

    private final ObjectProperty<Image> previewImage = new SimpleObjectProperty<>();
    private final ObjectProperty<TreeItem<SidebarTreeNode>> projectTreeRoot =
            new SimpleObjectProperty<>(new TreeItem<>(SidebarTreeNode.root("Project")));
    private final StringProperty viewportStatus = new SimpleStringProperty("Esperando preview");
    private final StringProperty metricsText = new SimpleStringProperty("");
    private final ObservableList<KeyframeDto> keyframes = FXCollections.observableArrayList();
    private final ObservableList<KeyframeTimelineItem> timelineItems = FXCollections.observableArrayList();
    private final ObjectProperty<DeepZoomAdvisory> pendingDeepZoomAdvisory = new SimpleObjectProperty<>();

    private double viewportWidth = 960.0;
    private double viewportHeight = 540.0;

    /**
     * Creates the shared shell view model used across the desktop UI.
     *
     * @param projectFacade application boundary for project editing and persistence
     * @param renderFacade application boundary for preview and render jobs
     * @param exportFacade application boundary for archive export
     * @param uiThreadExecutor JavaFX thread dispatcher
     * @param storageRoot base storage directory used by the desktop app
     */
    public StudioShellViewModel(
            ProjectFacade projectFacade,
            RenderFacade renderFacade,
            ExportFacade exportFacade,
            UiThreadExecutor uiThreadExecutor,
            Path storageRoot
    ) {
        this.projectFacade = projectFacade;
        this.renderFacade = renderFacade;
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
     * Replaces the current project with a new empty in-memory project.
     *
     * <p>The desktop session is intentionally ephemeral, so creating a new
     * project resets the visible state without silently restoring previous
     * internal workspaces.
     */
    public void createNewProject() {
        createNewProject(true);
    }

    private void createNewProject(boolean shouldRequestPreview) {
        projectSessionState.setCurrentProject(pointsTimelineCoordinator.setProject(
                projectFacade.createProject("Fractal Render Studio"),
                true
        ));
        projectSessionState.clearCurrentProjectFilePath();
        projectSessionState.resetCamera();
        renderQueueState.clear();
        resetPreviewSurface();
        appendMetric("Proyecto creado: " + projectSessionState.currentProject().name().value());
        refreshDerivedState();
        if (shouldRequestPreview) {
            requestAutoPreview();
        }
    }

    /**
     * Discards in-memory session state and removes internal runtime artifacts.
     *
     * <p>This method is invoked during shutdown so the application behaves like
     * a clean studio session instead of an autosaving scratchpad.
     */
    public void shutdownSession() {
        previewRefreshDebounce.stop();
        previewCoordinator.invalidate();
        pendingDeepZoomAdvisory.set(null);
        thumbnailGeneration.incrementAndGet();
        bookmarkThumbnailGeneration.incrementAndGet();
        previewImage.set(null);
        renderQueueState.clear();
        bookmarkSidebarItemsById.clear();
        timelineItems.clear();
        keyframes.clear();
        purgeSessionStorage();
    }

    /**
     * Captures the current camera as a reusable point and keeps the sidebar and
     * animation timeline in sync by creating both the saved point and its
     * timeline counterpart with the same label.
     *
     * <p>This is the place where the product decision "points and timeline
     * entries should behave as one concept" is enforced operationally.
     */
    public void addPoint() {
        applyPointMutation(pointsTimelineCoordinator.addPoint(projectFacade, projectSessionState.currentCameraState()));
    }

    /**
     * Captures the current camera state as a timeline point.
     *
     * <p>The method delegates to {@link #addPoint()} because the product treats
     * both names as a single user concept.
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
        applyPointMutation(pointsTimelineCoordinator.renameKeyframe(projectFacade, keyframeId, newLabel));
    }

    /**
     * Deletes a keyframe from the project timeline.
     *
     * @param keyframeId keyframe identifier
     */
    public void deleteKeyframe(String keyframeId) {
        applyPointMutation(pointsTimelineCoordinator.deleteKeyframe(projectFacade, keyframeId));
    }

    public void addBookmark() {
        addPoint();
    }

    public void deleteBookmark(String bookmarkId) {
        applyPointMutation(pointsTimelineCoordinator.deleteBookmark(projectFacade, bookmarkId));
    }

    /**
     * Renames a stored mathematical bookmark without altering its camera state.
     *
     * @param bookmarkId bookmark identifier
     * @param newLabel user-facing label to persist
     */
    public void renameBookmark(String bookmarkId, String newLabel) {
        applyPointMutation(pointsTimelineCoordinator.renameBookmark(projectFacade, bookmarkId, newLabel));
    }

    /**
     * Reorders a bookmark inside the sidebar list.
     *
     * @param bookmarkId bookmark identifier
     * @param direction negative for up, positive for down
     */
    public void moveBookmark(String bookmarkId, int direction) {
        applyPointMutation(pointsTimelineCoordinator.moveBookmark(projectFacade, bookmarkId, direction));
    }

    public void createKeyframeFromBookmark(String bookmarkId) {
        applyPointMutation(pointsTimelineCoordinator.createKeyframeFromBookmark(projectFacade, bookmarkId));
    }

    /**
     * Submits a render using the current project defaults for duration, FPS,
     * output workspace and preset.
     *
     * <p>This convenience entry point is what the shell uses when the user wants
     * a straightforward render flow without manually overriding every parameter.
     */
    public void submitRender() {
        double durationSeconds = currentSuggestedRenderDurationSeconds();
        submitRender(
                currentSuggestedRenderWorkspaceName(),
                estimateRenderFrameCount(durationSeconds, projectSessionState.currentProject().settings().defaultFramesPerSecond()),
                projectSessionState.currentProject().settings().defaultFramesPerSecond(),
                defaultRenderWorkspaceBaseDirectory(),
                projectSessionState.currentProject().settings().defaultRenderPreset().name()
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
        double planeHeight = 3.0 / projectSessionState.currentCameraState().zoomLevel().value();
        double planeWidth = planeHeight * (viewportWidth / viewportHeight);
        double deltaX = (deltaPixelsX / viewportWidth) * planeWidth;
        double deltaY = (deltaPixelsY / viewportHeight) * planeHeight;

        projectSessionState.setCurrentCameraState(projectSessionState.currentCameraState().pan(-deltaX, -deltaY));
        refreshCameraState();
    }

    /**
     * Multiplies the camera zoom by the provided factor.
     *
     * @param factor zoom multiplier
     */
    public boolean zoomCamera(double factor) {
        ZoomLimitPolicy.ZoomLimitDecision zoomDecision = ZoomLimitPolicy.evaluate(
                projectSessionState.currentProject().fractalFormula(),
                projectSessionState.currentCameraState().zoomLevel().valueDecimal(),
                factor
        );
        if (zoomDecision.blocked()) {
            handleZoomLimitReached();
            return false;
        }
        projectSessionState.setCurrentCameraState(new CameraState(
                projectSessionState.currentCameraState().center(),
                new ZoomLevel(zoomDecision.targetZoom())
        ));
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
                projectSessionState.currentProject().fractalFormula(),
                projectSessionState.currentCameraState().zoomLevel().valueDecimal(),
                factor
        );
        if (zoomDecision.blocked()) {
            handleZoomLimitReached();
            return false;
        }
        double normalizedAnchorX = clamp(anchorPixelX / viewportWidth, 0.0, 1.0);
        double normalizedAnchorY = clamp(anchorPixelY / viewportHeight, 0.0, 1.0);

        BigDecimal currentZoom = projectSessionState.currentCameraState().zoomLevel().valueDecimal();
        BigDecimal newZoom = zoomDecision.targetZoom();
        BigDecimal aspectRatio = BigDecimal.valueOf(viewportWidth / viewportHeight);

        BigDecimal currentPlaneHeight = BigDecimal.valueOf(3.0).divide(currentZoom, CAMERA_MATH_CONTEXT);
        BigDecimal currentPlaneWidth = currentPlaneHeight.multiply(aspectRatio, CAMERA_MATH_CONTEXT);
        BigDecimal newPlaneHeight = BigDecimal.valueOf(3.0).divide(newZoom, CAMERA_MATH_CONTEXT);
        BigDecimal newPlaneWidth = newPlaneHeight.multiply(aspectRatio, CAMERA_MATH_CONTEXT);

        BigDecimal currentMinX = projectSessionState.currentCameraState().center().xDecimal().subtract(
                currentPlaneWidth.divide(BigDecimal.valueOf(2.0), CAMERA_MATH_CONTEXT),
                CAMERA_MATH_CONTEXT
        );
        BigDecimal currentMinY = projectSessionState.currentCameraState().center().yDecimal().subtract(
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

        projectSessionState.setCurrentCameraState(new CameraState(
                new FractalCoordinate(newCenterX, newCenterY),
                new ZoomLevel(newZoom)
        ));
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
        projectSessionState.resetCamera();
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
        if (projectSessionState.currentProjectFilePath() == null) {
            appendMetric("Usa \"Guardar proyecto...\" para exportar el proyecto a un archivo");
            return;
        }
        saveProjectTo(projectSessionState.currentProjectFilePath());
    }

    public void saveProjectTo(Path projectPath) {
        try {
            Files.createDirectories(projectPath.toAbsolutePath().getParent());
            projectFacade.saveProject(projectSessionState.currentProject(), projectPath);
            projectSessionState.setCurrentProjectFilePath(projectPath);
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
            projectSessionState.setCurrentProject(pointsTimelineCoordinator.setProject(projectFacade.loadProject(projectPath), true));
            projectSessionState.setCurrentProjectFilePath(projectPath);
            projectSessionState.setCurrentCameraState(projectSessionState.resolveInitialCameraState(projectSessionState.currentProject(), false));
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
            renderQueueState.refresh(renderFacade.listRenderJobs());
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
        Project updatedProject = projectFacade.renameProject(projectSessionState.currentProject(), newProjectName);
        updatedProject = projectFacade.updateProjectDescription(updatedProject, description);
        updatedProject = projectFacade.updateProjectSettings(
                updatedProject,
                defaultFramesPerSecond,
                estimateRenderFrameCount(defaultDurationSeconds, defaultFramesPerSecond),
                keyframeStepSeconds,
                RenderPreset.valueOf(defaultRenderPreset)
        );
        projectSessionState.setCurrentProject(pointsTimelineCoordinator.setProject(updatedProject, false));
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
        return projectSessionState.currentProject().fractalFormula().name();
    }

    public String currentColorProfileName() {
        return projectSessionState.currentProject().colorProfile().name();
    }

    public int currentMaxIterations() {
        return projectSessionState.currentProject().renderProfile().escapeParameters().maxIterations();
    }

    public double currentEscapeRadius() {
        return projectSessionState.currentProject().renderProfile().escapeParameters().escapeRadius();
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
        Project updatedProject = projectFacade.updateInspector(
                projectSessionState.currentProject(),
                fractalFormulaType,
                colorProfileName,
                Math.max(32, Math.min(255, maxIterations)),
                Math.max(2.0, escapeRadius)
        );
        projectSessionState.setCurrentProject(pointsTimelineCoordinator.setProject(updatedProject, false));
        refreshDerivedState();
        appendMetric("Inspector aplicado: " + fractalFormulaName + " | " + colorProfileName
                + " | " + projectSessionState.currentProject().renderProfile().escapeParameters().maxIterations() + " iteraciones");
        requestAutoPreview();
    }

    public void submitRender(String renderName, int totalFrames, double framesPerSecond, Path baseDirectory, String renderPreset) {
        projectSessionState.currentProject().validateRenderability();
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
                projectSessionState.currentProject(),
                projectSessionState.currentCameraState(),
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
        return renderQueueState.workspaceDrawerVisibleProperty();
    }

    public ReadOnlyObjectProperty<WorkspaceDrawerTab> workspaceDrawerTabProperty() {
        return renderQueueState.workspaceDrawerTabProperty();
    }

    public void togglePointsDrawer() {
        renderQueueState.togglePointsDrawer();
    }

    public void openRenderQueue() {
        renderQueueState.openRenderQueue();
    }

    public void closeWorkspaceDrawer() {
        renderQueueState.closeWorkspaceDrawer();
    }

    public String currentProjectName() {
        return projectSessionState.currentProject().name().value();
    }

    public String currentProjectDescription() {
        return projectSessionState.currentProject().metadata().description();
    }

    public double currentDefaultFramesPerSecond() {
        return projectSessionState.currentProject().settings().defaultFramesPerSecond();
    }

    public int currentDefaultRenderFrameCount() {
        return projectSessionState.currentProject().settings().defaultRenderFrameCount();
    }

    public double currentSuggestedRenderDurationSeconds() {
        double timelineDuration = projectSessionState.currentProject().timeline().keyframes().isEmpty()
                ? 0.0
                : projectSessionState.currentProject().timeline().keyframes().getLast().timePosition().seconds();
        double defaultDuration = projectSessionState.currentProject().settings().defaultRenderFrameCount()
                / projectSessionState.currentProject().settings().defaultFramesPerSecond();
        return Math.max(1.0, Math.max(timelineDuration, defaultDuration));
    }

    public double currentKeyframeStepSeconds() {
        return projectSessionState.currentProject().settings().keyframeStepSeconds();
    }

    public String currentDefaultRenderPreset() {
        return projectSessionState.currentProject().settings().defaultRenderPreset().name();
    }

    public Path defaultRenderWorkspaceBaseDirectory() {
        Path desktopDirectory = Path.of(System.getProperty("user.home"), "Desktop");
        if (Files.isDirectory(desktopDirectory)) {
            return desktopDirectory;
        }
        return Path.of(System.getProperty("user.home"));
    }

    public String currentSuggestedRenderWorkspaceName() {
        return projectSessionState.currentProject().name().value() + " Render";
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
        return pointsTimelineCoordinator.currentBookmarkLabel(bookmarkId);
    }

    /**
     * Returns the persisted label of a keyframe.
     *
     * @param keyframeId keyframe identifier
     * @return keyframe label or an empty string when not found
     */
    public String currentKeyframeLabel(String keyframeId) {
        return pointsTimelineCoordinator.currentKeyframeLabel(keyframeId);
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
        return renderQueueState.renderJobs();
    }

    public ReadOnlyObjectProperty<TreeItem<SidebarTreeNode>> projectTreeRootProperty() {
        return projectTreeRoot;
    }

    public ReadOnlyStringProperty projectNameProperty() {
        return inspectorState.projectNameProperty();
    }

    public ReadOnlyStringProperty formulaNameProperty() {
        return inspectorState.formulaNameProperty();
    }

    public ReadOnlyStringProperty colorProfileNameProperty() {
        return inspectorState.colorProfileNameProperty();
    }

    public ReadOnlyStringProperty renderProfileNameProperty() {
        return inspectorState.renderProfileNameProperty();
    }

    public ReadOnlyStringProperty resolutionLabelProperty() {
        return inspectorState.resolutionLabelProperty();
    }

    public ReadOnlyStringProperty cameraCenterLabelProperty() {
        return inspectorState.cameraCenterLabelProperty();
    }

    public ReadOnlyStringProperty cameraZoomLabelProperty() {
        return inspectorState.cameraZoomLabelProperty();
    }

    public ReadOnlyStringProperty previewIterationsLabelProperty() {
        return inspectorState.previewIterationsLabelProperty();
    }

    public ReadOnlyStringProperty previewModeLabelProperty() {
        return inspectorState.previewModeLabelProperty();
    }

    public ReadOnlyStringProperty deepZoomHealthLabelProperty() {
        return inspectorState.deepZoomHealthLabelProperty();
    }

    public ReadOnlyStringProperty deepZoomMemoryLabelProperty() {
        return inspectorState.deepZoomMemoryLabelProperty();
    }

    public ReadOnlyIntegerProperty configuredMaxIterationsProperty() {
        return inspectorState.configuredMaxIterationsProperty();
    }

    public ReadOnlyDoubleProperty configuredEscapeRadiusProperty() {
        return inspectorState.configuredEscapeRadiusProperty();
    }

    public ReadOnlyStringProperty defaultRenderPresetLabelProperty() {
        return inspectorState.defaultRenderPresetLabelProperty();
    }

    public ReadOnlyStringProperty bookmarkSummaryProperty() {
        return inspectorState.bookmarkSummaryProperty();
    }

    public ReadOnlyStringProperty timelineSummaryProperty() {
        return inspectorState.timelineSummaryProperty();
    }

    public ReadOnlyStringProperty projectDescriptionProperty() {
        return inspectorState.projectDescriptionProperty();
    }

    public ReadOnlyStringProperty projectCreatedAtProperty() {
        return inspectorState.projectCreatedAtProperty();
    }

    public ReadOnlyStringProperty projectUpdatedAtProperty() {
        return inspectorState.projectUpdatedAtProperty();
    }

    public ReadOnlyStringProperty projectDefaultFpsProperty() {
        return inspectorState.projectDefaultFpsProperty();
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
            renderQueueState.acceptUpdate(statusDto, ignored -> { });
            persistRenderHistory();
            appendMetric(statusDto.jobName() + " -> " + statusDto.state().name() + " (" + statusDto.completedFrames()
                    + "/" + statusDto.totalFrames() + ")");
        });
    }

    private void refreshDerivedState() {
        projectSessionState.setCurrentProject(pointsTimelineCoordinator.setProject(projectSessionState.currentProject(), false));
        applyProjectPresentation(StudioProjectPresentation.from(
                projectSessionState.currentProject(),
                projectSessionState.currentCameraState(),
                projectFacade
        ));
    }

    private void applyProjectPresentation(StudioProjectPresentation presentation) {
        inspectorState.applyProjectPresentation(presentation, projectSessionState.currentProject(), captureMemoryPressure());
        keyframes.setAll(presentation.keyframes());
        timelineItems.setAll(pointsTimelineCoordinator.buildTimelineItems(presentation.keyframes()));
        projectTreeRoot.set(presentation.projectTreeRoot());
        rebuildBookmarkSidebarItems();
        regenerateTimelineThumbnails();
        regenerateBookmarkThumbnails();
    }

    private void refreshCameraState() {
        inspectorState.refreshCameraState(projectSessionState.currentCameraState(), projectSessionState.currentProject());
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

    private void applyPointMutation(StudioPointsTimelineCoordinator.PointMutation mutation) {
        if (!mutation.changed()) {
            return;
        }
        projectSessionState.setCurrentProject(mutation.project());
        refreshDerivedState();
        if (mutation.metricMessage() != null) {
            appendMetric(mutation.metricMessage());
        }
        if (mutation.requestAutoPreview()) {
            requestAutoPreview();
        }
    }

    private void applyFocusChange(StudioPointsTimelineCoordinator.FocusChange focusChange) {
        if (!focusChange.found()) {
            if (focusChange.metricMessage() != null) {
                appendMetric(focusChange.metricMessage());
            }
            return;
        }
        projectSessionState.setCurrentCameraState(focusChange.cameraState());
        refreshCameraState();
        if (focusChange.metricMessage() != null) {
            appendMetric(focusChange.metricMessage());
        }
        if (focusChange.requestAutoPreview()) {
            requestAutoPreview();
        }
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
        String fallbackName = projectSessionState.currentProject() == null
                ? "fractal-render"
                : projectSessionState.currentProject().name().value() + "-render";
        String rawName = renderName == null || renderName.isBlank() ? fallbackName : renderName.trim();
        String normalized = rawName.replaceAll("[^a-zA-Z0-9-_]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("(^-+)|(-+$)", "");
        return normalized.isBlank() ? "fractal-render" : normalized;
    }

    private String resolveRenderJobName(String renderName) {
        String fallbackName = projectSessionState.currentProject() == null
                ? "Fractal Render"
                : projectSessionState.currentProject().name().value() + " Render";
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
        projectFacade.saveProject(projectSessionState.currentProject(), projectSnapshotPath);
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
                projectSessionState.currentCameraState().zoomLevel().value(),
                projectSessionState.currentProject().renderProfile().escapeParameters().maxIterations(),
                precise
        );
        inspectorState.applyPreviewPlan(previewPlan.statusLabel(), previewPlan.maxIterations());
        DeepZoomAdvisory deepZoomAdvisory = DeepZoomAdvisor.evaluate(
                projectSessionState.currentCameraState().zoomLevel().value(),
                previewPlan.maxIterations(),
                viewportWidth,
                viewportHeight,
                precise,
                previewPlan.highPrecisionEnabled(),
                captureMemoryPressure()
        );
        inspectorState.applyDeepZoomAdvisory(deepZoomAdvisory);
        if (manual && deepZoomAdvisory.showDialog()) {
            pendingDeepZoomAdvisory.set(deepZoomAdvisory);
        }
        previewCoordinator.request(projectSessionState.currentProject(), projectSessionState.currentCameraState(), previewPlan);
    }

    private void rebuildBookmarkSidebarItems() {
        bookmarkSidebarItemsById.clear();
        for (ProjectBookmark bookmark : projectSessionState.currentProject().bookmarks()) {
            bookmarkSidebarItemsById.put(
                    bookmark.id().value(),
                    new BookmarkSidebarItem(bookmark.id().value(), bookmark.cameraState())
            );
        }
    }

    private void resetPreviewSurface() {
        previewCoordinator.invalidate();
        previewImage.set(null);
        inspectorState.resetPreviewState(captureMemoryPressure());
        viewportStatus.set("Listo para renderizar");
    }

    private void nudgeCamera(double horizontalStep, double verticalStep) {
        double planeHeight = 3.0 / projectSessionState.currentCameraState().zoomLevel().value();
        double planeWidth = planeHeight * (viewportWidth / viewportHeight);
        projectSessionState.setCurrentCameraState(projectSessionState.currentCameraState().pan(
                planeWidth * horizontalStep,
                planeHeight * verticalStep
        ));
        refreshCameraState();
        requestPreviewAfterInteraction();
    }

    private void jumpToBookmark(int direction) {
        applyFocusChange(pointsTimelineCoordinator.jumpToBookmark(direction));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Estimates how many discrete frames are required for a given animation
     * duration and frame rate.
     *
     * <p>The calculation keeps at least one frame and adds the terminal frame so
     * that the end state of the camera path is not dropped by rounding.
     */
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
        viewportStatus.set("Limite de zoom alcanzado para " + projectSessionState.currentProject().fractalFormula().name());
        appendMetric("Limite de zoom alcanzado (" + ZoomLimitPolicy.maxZoomFor(projectSessionState.currentProject().fractalFormula()) + ")");
    }

    public void focusKeyframe(String keyframeId) {
        applyFocusChange(pointsTimelineCoordinator.focusKeyframe(keyframeId));
    }

    /**
     * Loads a saved bookmark camera into the viewport and refreshes the preview.
     *
     * @param bookmarkId bookmark identifier from the sidebar or navigation controls
     */
    public void focusBookmark(String bookmarkId) {
        applyFocusChange(pointsTimelineCoordinator.focusBookmark(bookmarkId));
    }

    private void regenerateTimelineThumbnails() {
        long generation = thumbnailGeneration.incrementAndGet();
        if (timelineItems.isEmpty()) {
            return;
        }
        for (KeyframeTimelineItem timelineItem : timelineItems) {
            timelineItem.setThumbnail(null);
            timelineItem.setThumbnailStatus("Generando miniatura...");
            renderFacade.generateKeyframeThumbnail(projectSessionState.currentProject(), timelineItem.cameraState())
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
            renderFacade.generateCameraThumbnail(projectSessionState.currentProject(), bookmarkItem.cameraState())
                    .thenAccept(renderedFrame -> uiThreadExecutor.execute(() -> {
                        if (generation != bookmarkThumbnailGeneration.get()) {
                            return;
                        }
                        bookmarkItem.setThumbnail(convertToImage(renderedFrame));
                    }))
                    .exceptionally(throwable -> null);
        }
    }
}
