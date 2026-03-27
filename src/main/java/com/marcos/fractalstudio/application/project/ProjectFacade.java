package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.application.dto.KeyframeDto;
import com.marcos.fractalstudio.application.timeline.AddKeyframeUseCase;
import com.marcos.fractalstudio.application.timeline.DeleteKeyframeUseCase;
import com.marcos.fractalstudio.application.timeline.RenameKeyframeUseCase;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaType;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.RenderPreset;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Stable application boundary for project-centric operations used by the UI layer.
 */
public final class ProjectFacade {

    private final CreateProjectUseCase createProjectUseCase;
    private final AddKeyframeUseCase addKeyframeUseCase;
    private final RenameKeyframeUseCase renameKeyframeUseCase;
    private final DeleteKeyframeUseCase deleteKeyframeUseCase;
    private final AddBookmarkUseCase addBookmarkUseCase;
    private final DeleteBookmarkUseCase deleteBookmarkUseCase;
    private final RenameBookmarkUseCase renameBookmarkUseCase;
    private final MoveBookmarkUseCase moveBookmarkUseCase;
    private final CreateKeyframeFromBookmarkUseCase createKeyframeFromBookmarkUseCase;
    private final UpdateProjectInspectorUseCase updateProjectInspectorUseCase;
    private final RenameProjectUseCase renameProjectUseCase;
    private final UpdateProjectMetadataUseCase updateProjectMetadataUseCase;
    private final UpdateProjectSettingsUseCase updateProjectSettingsUseCase;
    private final SaveProjectUseCase saveProjectUseCase;
    private final LoadProjectUseCase loadProjectUseCase;

    public ProjectFacade(
            CreateProjectUseCase createProjectUseCase,
            AddKeyframeUseCase addKeyframeUseCase,
            RenameKeyframeUseCase renameKeyframeUseCase,
            DeleteKeyframeUseCase deleteKeyframeUseCase,
            AddBookmarkUseCase addBookmarkUseCase,
            DeleteBookmarkUseCase deleteBookmarkUseCase,
            RenameBookmarkUseCase renameBookmarkUseCase,
            MoveBookmarkUseCase moveBookmarkUseCase,
            CreateKeyframeFromBookmarkUseCase createKeyframeFromBookmarkUseCase,
            UpdateProjectInspectorUseCase updateProjectInspectorUseCase,
            RenameProjectUseCase renameProjectUseCase,
            UpdateProjectMetadataUseCase updateProjectMetadataUseCase,
            UpdateProjectSettingsUseCase updateProjectSettingsUseCase,
            SaveProjectUseCase saveProjectUseCase,
            LoadProjectUseCase loadProjectUseCase
    ) {
        this.createProjectUseCase = createProjectUseCase;
        this.addKeyframeUseCase = addKeyframeUseCase;
        this.renameKeyframeUseCase = renameKeyframeUseCase;
        this.deleteKeyframeUseCase = deleteKeyframeUseCase;
        this.addBookmarkUseCase = addBookmarkUseCase;
        this.deleteBookmarkUseCase = deleteBookmarkUseCase;
        this.renameBookmarkUseCase = renameBookmarkUseCase;
        this.moveBookmarkUseCase = moveBookmarkUseCase;
        this.createKeyframeFromBookmarkUseCase = createKeyframeFromBookmarkUseCase;
        this.updateProjectInspectorUseCase = updateProjectInspectorUseCase;
        this.renameProjectUseCase = renameProjectUseCase;
        this.updateProjectMetadataUseCase = updateProjectMetadataUseCase;
        this.updateProjectSettingsUseCase = updateProjectSettingsUseCase;
        this.saveProjectUseCase = saveProjectUseCase;
        this.loadProjectUseCase = loadProjectUseCase;
    }

    /**
     * Creates a new project aggregate with default settings.
     *
     * @param name project display name
     * @return initialized project
     */
    public Project createProject(String name) {
        return createProjectUseCase.create(name);
    }

    public Project addKeyframe(Project project, CameraState cameraState) {
        return addKeyframeUseCase.add(project, cameraState);
    }

    public Project renameKeyframe(Project project, String keyframeId, String newLabel) {
        return renameKeyframeUseCase.rename(project, keyframeId, newLabel);
    }

    public Project deleteKeyframe(Project project, String keyframeId) {
        return deleteKeyframeUseCase.delete(project, keyframeId);
    }

    public Project addBookmark(Project project, CameraState cameraState) {
        return addBookmarkUseCase.add(project, cameraState);
    }

    public Project deleteBookmark(Project project, String bookmarkId) {
        return deleteBookmarkUseCase.delete(project, bookmarkId);
    }

    public Project renameBookmark(Project project, String bookmarkId, String newLabel) {
        return renameBookmarkUseCase.rename(project, bookmarkId, newLabel);
    }

    public Project moveBookmark(Project project, String bookmarkId, int direction) {
        return moveBookmarkUseCase.move(project, bookmarkId, direction);
    }

    public Project createKeyframeFromBookmark(Project project, String bookmarkId) {
        return createKeyframeFromBookmarkUseCase.create(project, bookmarkId);
    }

    public Project updateInspector(
            Project project,
            FractalFormulaType fractalFormulaType,
            String colorProfileName,
            int maxIterations,
            double escapeRadius
    ) {
        return updateProjectInspectorUseCase.update(project, fractalFormulaType, colorProfileName, maxIterations, escapeRadius);
    }

    public Project renameProject(Project project, String newProjectName) {
        return renameProjectUseCase.rename(project, newProjectName);
    }

    public Project updateProjectDescription(Project project, String description) {
        return updateProjectMetadataUseCase.updateDescription(project, description);
    }

    /**
     * Replaces the current project settings with a validated version.
     *
     * @param project project being edited
     * @param defaultFramesPerSecond default playback frame rate
     * @param defaultRenderFrameCount default render frame count
     * @param keyframeStepSeconds default spacing for generated keyframes
     * @param defaultRenderPreset default render quality preset
     * @return updated project aggregate
     */
    public Project updateProjectSettings(
            Project project,
            double defaultFramesPerSecond,
            int defaultRenderFrameCount,
            double keyframeStepSeconds,
            RenderPreset defaultRenderPreset
    ) {
        return updateProjectSettingsUseCase.update(
                project,
                defaultFramesPerSecond,
                defaultRenderFrameCount,
                keyframeStepSeconds,
                defaultRenderPreset
        );
    }

    /**
     * Converts a domain keyframe to a presentation-friendly DTO.
     *
     * @param keyframe domain keyframe
     * @return immutable DTO for the UI
     */
    public KeyframeDto toKeyframeDto(com.marcos.fractalstudio.domain.timeline.Keyframe keyframe) {
        return addKeyframeUseCase.toDto(keyframe);
    }

    /**
     * Persists a project snapshot to disk.
     *
     * @param project project aggregate to persist
     * @param projectPath destination path
     * @throws IOException when persistence fails
     */
    public void saveProject(Project project, Path projectPath) throws IOException {
        saveProjectUseCase.save(project, projectPath);
    }

    /**
     * Loads a project snapshot from disk.
     *
     * @param projectPath source path
     * @return restored project aggregate
     * @throws IOException when the snapshot cannot be read
     */
    public Project loadProject(Path projectPath) throws IOException {
        return loadProjectUseCase.load(projectPath);
    }
}
