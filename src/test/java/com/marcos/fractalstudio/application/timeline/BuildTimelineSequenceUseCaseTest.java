package com.marcos.fractalstudio.application.timeline;

import com.marcos.fractalstudio.application.project.AddBookmarkUseCase;
import com.marcos.fractalstudio.application.project.CreateProjectUseCase;
import com.marcos.fractalstudio.application.project.ProjectFacade;
import com.marcos.fractalstudio.application.project.ProjectRepository;
import com.marcos.fractalstudio.application.project.SaveProjectUseCase;
import com.marcos.fractalstudio.application.project.LoadProjectUseCase;
import com.marcos.fractalstudio.application.project.RenameBookmarkUseCase;
import com.marcos.fractalstudio.application.project.MoveBookmarkUseCase;
import com.marcos.fractalstudio.application.project.DeleteBookmarkUseCase;
import com.marcos.fractalstudio.application.project.CreateKeyframeFromBookmarkUseCase;
import com.marcos.fractalstudio.application.project.UpdateProjectInspectorUseCase;
import com.marcos.fractalstudio.application.project.RenameProjectUseCase;
import com.marcos.fractalstudio.application.project.UpdateProjectMetadataUseCase;
import com.marcos.fractalstudio.application.project.UpdateProjectSettingsUseCase;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;
import com.marcos.fractalstudio.domain.render.RenderPreset;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class BuildTimelineSequenceUseCaseTest {

    @Test
    void fallsBackToSavedPointsWhenTimelineIsTooShortForAnimation() {
        ProjectFacade projectFacade = createProjectFacade();
        BuildTimelineSequenceUseCase useCase = new BuildTimelineSequenceUseCase();

        CameraState firstPoint = new CameraState(new FractalCoordinate(-0.72, 0.11), new ZoomLevel(18.0));
        CameraState secondPoint = new CameraState(new FractalCoordinate(-0.61, -0.44), new ZoomLevel(240.0));

        Project project = projectFacade.createProject("Demo");
        project = projectFacade.addKeyframe(project, firstPoint);
        project = projectFacade.addBookmark(project, firstPoint);
        project = projectFacade.renameBookmark(project, project.bookmarks().getFirst().id().value(), "P-1");
        project = projectFacade.addBookmark(project, secondPoint);
        project = projectFacade.renameBookmark(project, project.bookmarks().getLast().id().value(), "P-2");

        List<FrameDescriptor> frameDescriptors = useCase.build(project, firstPoint, 48, 24.0, RenderPreset.STANDARD);

        CameraState firstFrame = frameDescriptors.getFirst().cameraState();
        CameraState midFrame = frameDescriptors.get(frameDescriptors.size() / 2).cameraState();
        CameraState lastFrame = frameDescriptors.getLast().cameraState();

        assertEquals(firstPoint.center().xDecimal(), firstFrame.center().xDecimal());
        assertEquals(firstPoint.center().yDecimal(), firstFrame.center().yDecimal());
        assertEquals(secondPoint.center().xDecimal(), lastFrame.center().xDecimal());
        assertEquals(secondPoint.center().yDecimal(), lastFrame.center().yDecimal());
        assertNotEquals(firstFrame.center().xDecimal(), midFrame.center().xDecimal());
        assertNotEquals(firstFrame.zoomLevel().valueDecimal(), midFrame.zoomLevel().valueDecimal());
    }

    private ProjectFacade createProjectFacade() {
        ProjectRepository projectRepository = new ProjectRepository() {
            @Override
            public void save(Path projectPath, Project project) throws IOException {
            }

            @Override
            public Project load(Path projectPath) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
        AddKeyframeUseCase addKeyframeUseCase = new AddKeyframeUseCase();
        return new ProjectFacade(
                new CreateProjectUseCase(),
                addKeyframeUseCase,
                new RenameKeyframeUseCase(),
                new DeleteKeyframeUseCase(),
                new AddBookmarkUseCase(),
                new DeleteBookmarkUseCase(),
                new RenameBookmarkUseCase(),
                new MoveBookmarkUseCase(),
                new CreateKeyframeFromBookmarkUseCase(addKeyframeUseCase),
                new UpdateProjectInspectorUseCase(),
                new RenameProjectUseCase(),
                new UpdateProjectMetadataUseCase(),
                new UpdateProjectSettingsUseCase(),
                new SaveProjectUseCase(projectRepository),
                new LoadProjectUseCase(projectRepository)
        );
    }
}
