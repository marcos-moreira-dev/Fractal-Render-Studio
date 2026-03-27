package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.render.RenderPreset;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ProjectFacadeTest {

    private static ProjectFacade createProjectFacade(ProjectRepository projectRepository) {
        com.marcos.fractalstudio.application.timeline.AddKeyframeUseCase addKeyframeUseCase =
                new com.marcos.fractalstudio.application.timeline.AddKeyframeUseCase();
        return new ProjectFacade(
                new CreateProjectUseCase(),
                addKeyframeUseCase,
                new com.marcos.fractalstudio.application.timeline.RenameKeyframeUseCase(),
                new com.marcos.fractalstudio.application.timeline.DeleteKeyframeUseCase(),
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

    @Test
    void updatesProjectIdentityMetadataAndSettings() {
        ProjectRepository projectRepository = new ProjectRepository() {
            @Override
            public void save(Path projectPath, Project project) throws IOException {
            }

            @Override
            public Project load(Path projectPath) throws IOException {
                throw new UnsupportedOperationException();
            }
        };

        ProjectFacade projectFacade = createProjectFacade(projectRepository);

        Project project = projectFacade.createProject("Original");
        project = projectFacade.renameProject(project, "Nuevo Nombre");
        project = projectFacade.updateProjectDescription(project, "Descripcion");
        project = projectFacade.updateProjectSettings(project, 12.0, 60, 1.5, RenderPreset.DEEP_ZOOM);

        assertEquals("Nuevo Nombre", project.name().value());
        assertEquals("Descripcion", project.metadata().description());
        assertEquals(12.0, project.settings().defaultFramesPerSecond());
        assertEquals(60, project.settings().defaultRenderFrameCount());
        assertEquals(1.5, project.settings().keyframeStepSeconds());
        assertEquals(RenderPreset.DEEP_ZOOM, project.settings().defaultRenderPreset());
    }

    @Test
    void updatesInspectorControlledFractalParameters() {
        ProjectRepository projectRepository = new ProjectRepository() {
            @Override
            public void save(Path projectPath, Project project) throws IOException {
            }

            @Override
            public Project load(Path projectPath) throws IOException {
                throw new UnsupportedOperationException();
            }
        };

        ProjectFacade projectFacade = createProjectFacade(projectRepository);

        Project project = projectFacade.createProject("Original");
        project = projectFacade.updateInspector(
                project,
                com.marcos.fractalstudio.domain.fractal.FractalFormulaType.BURNING_SHIP,
                "Electric Ice",
                255,
                6.0
        );

        assertEquals("Burning Ship", project.fractalFormula().name());
        assertEquals("Electric Ice", project.colorProfile().name());
        assertEquals(255, project.renderProfile().escapeParameters().maxIterations());
        assertEquals(6.0, project.renderProfile().escapeParameters().escapeRadius());
    }

    @Test
    void convertsBookmarkIntoKeyframeAndAllowsDeletion() {
        ProjectRepository projectRepository = new ProjectRepository() {
            @Override
            public void save(Path projectPath, Project project) throws IOException {
            }

            @Override
            public Project load(Path projectPath) throws IOException {
                throw new UnsupportedOperationException();
            }
        };

        ProjectFacade projectFacade = createProjectFacade(projectRepository);
        Project project = projectFacade.createProject("Original");
        project = projectFacade.addBookmark(
                project,
                new com.marcos.fractalstudio.domain.camera.CameraState(
                        new com.marcos.fractalstudio.domain.camera.FractalCoordinate(-0.75, 0.12),
                        new com.marcos.fractalstudio.domain.camera.ZoomLevel(12.0)
                )
        );

        String bookmarkId = project.bookmarks().getFirst().id().value();
        project = projectFacade.createKeyframeFromBookmark(project, bookmarkId);

        assertEquals(1, project.timeline().keyframes().size());

        project = projectFacade.deleteBookmark(project, bookmarkId);

        assertEquals(0, project.bookmarks().size());
    }

    @Test
    void renamesAndReordersBookmarks() {
        ProjectRepository projectRepository = new ProjectRepository() {
            @Override
            public void save(Path projectPath, Project project) throws IOException {
            }

            @Override
            public Project load(Path projectPath) throws IOException {
                throw new UnsupportedOperationException();
            }
        };

        ProjectFacade projectFacade = createProjectFacade(projectRepository);
        Project project = projectFacade.createProject("Original");
        project = projectFacade.addBookmark(
                project,
                new com.marcos.fractalstudio.domain.camera.CameraState(
                        new com.marcos.fractalstudio.domain.camera.FractalCoordinate(-0.75, 0.12),
                        new com.marcos.fractalstudio.domain.camera.ZoomLevel(12.0)
                )
        );
        project = projectFacade.addBookmark(
                project,
                new com.marcos.fractalstudio.domain.camera.CameraState(
                        new com.marcos.fractalstudio.domain.camera.FractalCoordinate(-0.61, -0.44),
                        new com.marcos.fractalstudio.domain.camera.ZoomLevel(220.0)
                )
        );

        String firstBookmarkId = project.bookmarks().get(0).id().value();
        String secondBookmarkId = project.bookmarks().get(1).id().value();

        project = projectFacade.renameBookmark(project, secondBookmarkId, "Espiral profunda");
        project = projectFacade.moveBookmark(project, secondBookmarkId, -1);

        assertEquals("Espiral profunda", project.bookmarks().getFirst().label());
        assertEquals(secondBookmarkId, project.bookmarks().getFirst().id().value());
        assertEquals(firstBookmarkId, project.bookmarks().get(1).id().value());
    }

    @Test
    void renamesAndDeletesKeyframes() {
        ProjectRepository projectRepository = new ProjectRepository() {
            @Override
            public void save(Path projectPath, Project project) throws IOException {
            }

            @Override
            public Project load(Path projectPath) throws IOException {
                throw new UnsupportedOperationException();
            }
        };

        ProjectFacade projectFacade = createProjectFacade(projectRepository);
        Project project = projectFacade.createProject("Original");
        project = projectFacade.addKeyframe(
                project,
                new com.marcos.fractalstudio.domain.camera.CameraState(
                        new com.marcos.fractalstudio.domain.camera.FractalCoordinate(-0.75, 0.12),
                        new com.marcos.fractalstudio.domain.camera.ZoomLevel(12.0)
                )
        );

        String keyframeId = project.timeline().keyframes().getFirst().id().value();
        project = projectFacade.renameKeyframe(project, keyframeId, "Entrada profunda");

        assertEquals("Entrada profunda", project.timeline().keyframes().getFirst().label());

        project = projectFacade.deleteKeyframe(project, keyframeId);

        assertEquals(0, project.timeline().keyframes().size());
    }
}
