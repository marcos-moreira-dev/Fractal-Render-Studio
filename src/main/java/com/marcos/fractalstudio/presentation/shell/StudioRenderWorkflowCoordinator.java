package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.application.export.ExportFacade;
import com.marcos.fractalstudio.application.export.ExportRequest;
import com.marcos.fractalstudio.application.export.ExportResult;
import com.marcos.fractalstudio.application.render.RenderFacade;
import com.marcos.fractalstudio.application.render.RenderRequest;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.RenderPreset;
import com.marcos.fractalstudio.presentation.renderqueue.RenderJobRow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Coordinates the presentation-side workflow that turns user render choices
 * into concrete workspaces, render jobs and exported artifacts.
 *
 * <p>The class keeps path normalization, render workspace creation and export
 * mechanics out of the shell view model so that the shell can focus on UI
 * state, notifications and navigation instead of file-system choreography.
 */
final class StudioRenderWorkflowCoordinator {

    private final RenderFacade renderFacade;
    private final ExportFacade exportFacade;
    private final StudioStoragePaths storagePaths;

    /**
     * Creates the render workflow coordinator used by the desktop shell.
     *
     * @param renderFacade application boundary for submitting and observing render jobs
     * @param exportFacade application boundary for frame archive export
     * @param storagePaths session-level storage layout used by the desktop app
     */
    StudioRenderWorkflowCoordinator(
            RenderFacade renderFacade,
            ExportFacade exportFacade,
            StudioStoragePaths storagePaths
    ) {
        this.renderFacade = renderFacade;
        this.exportFacade = exportFacade;
        this.storagePaths = storagePaths;
    }

    /**
     * Returns the default base directory suggested to the user for render workspaces.
     *
     * <p>The desktop uses the system desktop when available and falls back to
     * the home directory otherwise.
     */
    Path defaultRenderWorkspaceBaseDirectory() {
        Path desktopDirectory = Path.of(System.getProperty("user.home"), "Desktop");
        if (Files.isDirectory(desktopDirectory)) {
            return desktopDirectory;
        }
        return Path.of(System.getProperty("user.home"));
    }

    /**
     * Builds the default human-readable workspace name suggested in render dialogs.
     *
     * @param project current project
     * @return suggested render workspace name
     */
    String suggestedRenderWorkspaceName(Project project) {
        return project.name().value() + " Render";
    }

    /**
     * Returns the default ZIP path suggested for frame export.
     *
     * @param renderJobRow render row selected in the queue
     * @return suggested archive path
     */
    Path suggestedArchivePath(RenderJobRow renderJobRow) {
        String safeName = renderJobRow.jobName().replaceAll("[^a-zA-Z0-9-_]+", "_");
        return storagePaths.exportStorageDirectory().resolve(safeName + ".zip");
    }

    /**
     * Returns the default MP4 path suggested for copying the generated video.
     *
     * @param renderJobRow render row selected in the queue
     * @return suggested destination path
     */
    Path suggestedVideoPath(RenderJobRow renderJobRow) {
        String safeName = renderJobRow.jobName().replaceAll("[^a-zA-Z0-9-_]+", "_");
        return storagePaths.exportStorageDirectory().resolve(safeName + ".mp4");
    }

    /**
     * Creates the filesystem workspace for a render and stores the project
     * snapshot that reproduces it.
     *
     * @param project project snapshot to persist
     * @param projectSaver callback used to write the project JSON
     * @param baseDirectory user-selected base directory
     * @param renderName human-readable render name
     * @param totalFrames frame count
     * @param framesPerSecond render FPS
     * @param renderPreset preset name used for the render
     * @param durationSeconds effective duration
     * @return prepared render workspace description
     * @throws IOException when workspace preparation fails
     */
    PreparedRenderWorkspace prepareRenderWorkspace(
            Project project,
            ProjectSaver projectSaver,
            Path baseDirectory,
            String renderName,
            int totalFrames,
            double framesPerSecond,
            String renderPreset,
            double durationSeconds
    ) throws IOException {
        String effectiveRenderName = resolveRenderJobName(project, renderName);
        Path outputDirectory = buildRenderOutputDirectory(project, baseDirectory, effectiveRenderName);
        Files.createDirectories(outputDirectory);
        Path projectSnapshotPath = outputDirectory.resolve("project.fractalstudio.json");
        projectSaver.save(project, projectSnapshotPath);
        String metricMessage = "Proyecto del render guardado en " + projectSnapshotPath + " | "
                + totalFrames + " frames a " + framesPerSecond + " fps | "
                + durationSeconds + " s | " + renderPreset + " | " + effectiveRenderName;
        return new PreparedRenderWorkspace(effectiveRenderName, outputDirectory, projectSnapshotPath, metricMessage);
    }

    /**
     * Submits a background render job using the prepared workspace.
     *
     * @param project current project snapshot
     * @param cameraState camera to render from
     * @param workspace prepared render workspace
     * @param totalFrames frame count
     * @param framesPerSecond render FPS
     * @param renderPreset preset name
     * @param statusConsumer status callback for queue updates
     */
    void submitRender(
            Project project,
            CameraState cameraState,
            PreparedRenderWorkspace workspace,
            int totalFrames,
            double framesPerSecond,
            String renderPreset,
            Consumer<com.marcos.fractalstudio.application.dto.RenderJobStatusDto> statusConsumer
    ) {
        renderFacade.submitRender(
                new RenderRequest(
                        project,
                        cameraState,
                        workspace.renderName(),
                        totalFrames,
                        framesPerSecond,
                        workspace.outputDirectory(),
                        RenderPreset.valueOf(renderPreset)
                ),
                statusConsumer
        );
    }

    /**
     * Exports the PNG frame sequence of a completed job into a ZIP archive.
     *
     * @param renderJobRow selected completed render row
     * @param archivePath destination ZIP path
     * @return archive export result
     * @throws IOException when export fails
     */
    ExportResult exportFrames(RenderJobRow renderJobRow, Path archivePath) throws IOException {
        Files.createDirectories(storagePaths.exportStorageDirectory());
        return exportFacade.exportFrames(new ExportRequest(
                resolveFramesDirectory(Path.of(renderJobRow.outputDirectory())),
                archivePath
        ));
    }

    /**
     * Copies the generated MP4 of a completed job to the chosen destination.
     *
     * @param renderJobRow selected completed render row
     * @param destinationVideo destination MP4 path
     * @throws IOException when the copy fails
     */
    void exportVideo(RenderJobRow renderJobRow, Path destinationVideo) throws IOException {
        Files.createDirectories(storagePaths.exportStorageDirectory());
        Path sourceVideo = Path.of(renderJobRow.outputDirectory()).resolve("render.mp4");
        if (!Files.exists(sourceVideo)) {
            throw new FileNotFoundException("No se encontro render.mp4 en la carpeta del trabajo.");
        }
        Files.copy(sourceVideo, destinationVideo, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path resolveFramesDirectory(Path outputDirectory) {
        Path framesDirectory = outputDirectory.resolve("frames");
        return Files.exists(framesDirectory) ? framesDirectory : outputDirectory;
    }

    private Path buildRenderOutputDirectory(Project project, Path baseDirectory, String renderName) {
        Path normalizedBaseDirectory = (baseDirectory == null ? defaultRenderWorkspaceBaseDirectory() : baseDirectory).toAbsolutePath();
        String safeName = normalizeRenderWorkspaceName(project, renderName);
        Path candidate = normalizedBaseDirectory.resolve(safeName);
        if (Files.notExists(candidate)) {
            return candidate;
        }
        return normalizedBaseDirectory.resolve(safeName + "-" + System.currentTimeMillis());
    }

    private String normalizeRenderWorkspaceName(Project project, String renderName) {
        String fallbackName = project.name().value() + "-render";
        String rawName = renderName == null || renderName.isBlank() ? fallbackName : renderName.trim();
        String normalized = rawName.replaceAll("[^a-zA-Z0-9-_]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("(^-+)|(-+$)", "");
        return normalized.isBlank() ? "fractal-render" : normalized;
    }

    private String resolveRenderJobName(Project project, String renderName) {
        String fallbackName = project.name().value() + " Render";
        return renderName == null || renderName.isBlank() ? fallbackName : renderName.trim();
    }

    /**
     * Functional contract used to persist a project snapshot while preparing a render workspace.
     */
    @FunctionalInterface
    interface ProjectSaver {
        void save(Project project, Path path) throws IOException;
    }

    /**
     * Immutable description of a filesystem workspace prepared for a render job.
     *
     * @param renderName effective human-readable render name
     * @param outputDirectory workspace directory created for the render
     * @param projectSnapshotPath project JSON stored next to the render artifacts
     * @param metricMessage ready-to-log/metric summary of the prepared workspace
     */
    record PreparedRenderWorkspace(
            String renderName,
            Path outputDirectory,
            Path projectSnapshotPath,
            String metricMessage
    ) {
    }
}
