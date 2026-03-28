package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;
import com.marcos.fractalstudio.domain.project.Project;

import java.nio.file.Path;

/**
 * Stores the mutable session-level project context used by the desktop shell.
 *
 * <p>This object groups the three mutable references that define the current working session:
 * the loaded project aggregate, the file path associated with it and the camera currently displayed
 * in the explorer. Keeping them together reduces the amount of cross-cutting state owned directly
 * by the shell view model.
 */
final class StudioProjectSessionState {

    static final CameraState DEFAULT_CAMERA_STATE = new CameraState(
            new FractalCoordinate(-0.5, 0.0),
            new ZoomLevel(1.0)
    );

    private Project currentProject;
    private Path currentProjectFilePath;
    private CameraState currentCameraState = DEFAULT_CAMERA_STATE;

    Project currentProject() {
        return currentProject;
    }

    void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }

    Path currentProjectFilePath() {
        return currentProjectFilePath;
    }

    void setCurrentProjectFilePath(Path currentProjectFilePath) {
        this.currentProjectFilePath = currentProjectFilePath;
    }

    void clearCurrentProjectFilePath() {
        this.currentProjectFilePath = null;
    }

    CameraState currentCameraState() {
        return currentCameraState;
    }

    void setCurrentCameraState(CameraState currentCameraState) {
        this.currentCameraState = currentCameraState;
    }

    void resetCamera() {
        this.currentCameraState = DEFAULT_CAMERA_STATE;
    }

    CameraState resolveInitialCameraState(Project project, boolean startupFraming) {
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
}
