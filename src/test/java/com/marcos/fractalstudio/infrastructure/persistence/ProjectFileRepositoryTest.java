package com.marcos.fractalstudio.infrastructure.persistence;

import com.marcos.fractalstudio.application.project.CreateProjectUseCase;
import com.marcos.fractalstudio.application.project.ProjectRepository;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaFactory;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaType;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;
import com.marcos.fractalstudio.domain.project.ProjectBookmarkId;
import com.marcos.fractalstudio.domain.render.RenderPreset;
import com.marcos.fractalstudio.domain.timeline.Keyframe;
import com.marcos.fractalstudio.domain.timeline.KeyframeId;
import com.marcos.fractalstudio.domain.timeline.TimePosition;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ProjectFileRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsProjectAsJson() throws IOException {
        ProjectRepository repository = new ProjectFileRepository();
        Project project = new CreateProjectUseCase().create("Proyecto Persistido")
                .withFractalFormula(FractalFormulaFactory.create(FractalFormulaType.TRICORN));
        Path projectPath = tempDir.resolve("project.json");

        repository.save(projectPath, project);
        Project loadedProject = repository.load(projectPath);

        assertEquals(project.name().value(), loadedProject.name().value());
        assertEquals("Tricorn", loadedProject.fractalFormula().name());
        assertEquals(project.renderProfile().resolution().width(), loadedProject.renderProfile().resolution().width());
        assertEquals(project.colorProfile().name(), loadedProject.colorProfile().name());
        assertEquals(project.settings().defaultFramesPerSecond(), loadedProject.settings().defaultFramesPerSecond());
        assertEquals(project.metadata().description(), loadedProject.metadata().description());
        assertEquals(project.settings().defaultRenderPreset(), loadedProject.settings().defaultRenderPreset());
        assertEquals(project.bookmarks().size(), loadedProject.bookmarks().size());
    }

    @Test
    void preservesHighPrecisionCameraValuesWhenProjectContainsKeyframes() throws IOException {
        ProjectRepository repository = new ProjectFileRepository();
        Project project = new CreateProjectUseCase().create("Proyecto Profundo");
        Project projectWithKeyframe = project.withTimeline(project.timeline().addKeyframe(new Keyframe(
                KeyframeId.create(),
                new TimePosition(2.0),
                new CameraState(
                        new FractalCoordinate(
                                new BigDecimal("-0.743643887037158704752191506114774"),
                                new BigDecimal("0.131825904205311970493132056385139")
                        ),
                        new ZoomLevel(new BigDecimal("1000000000000.1234567890123456789"))
                ),
                "Deep Zoom"
        )));
        Path projectPath = tempDir.resolve("high-precision-project.json");

        repository.save(projectPath, projectWithKeyframe);
        Project loadedProject = repository.load(projectPath);
        Keyframe loadedKeyframe = loadedProject.timeline().keyframes().getFirst();

        assertEquals("-0.743643887037158704752191506114774", loadedKeyframe.cameraState().center().xPlainString());
        assertEquals("0.131825904205311970493132056385139", loadedKeyframe.cameraState().center().yPlainString());
        assertEquals("1000000000000.1234567890123456789", loadedKeyframe.cameraState().zoomLevel().plainString());
    }

    @Test
    void preservesHighPrecisionBookmarks() throws IOException {
        ProjectRepository repository = new ProjectFileRepository();
        Project project = new CreateProjectUseCase().create("Proyecto Bookmarks");
        Project projectWithBookmark = project.withBookmarks(java.util.List.of(
                new ProjectBookmark(
                        ProjectBookmarkId.create(),
                        "BM-1",
                        new CameraState(
                                new FractalCoordinate(
                                        new BigDecimal("-0.743643887037158704752191506114774"),
                                        new BigDecimal("0.131825904205311970493132056385139")
                                ),
                                new ZoomLevel(new BigDecimal("1000000000000.1234567890123456789"))
                        )
                )
        ));
        Path projectPath = tempDir.resolve("bookmark-project.json");

        repository.save(projectPath, projectWithBookmark);
        Project loadedProject = repository.load(projectPath);
        ProjectBookmark loadedBookmark = loadedProject.bookmarks().getFirst();

        assertEquals("BM-1", loadedBookmark.label());
        assertEquals("-0.743643887037158704752191506114774", loadedBookmark.cameraState().center().xPlainString());
        assertEquals("0.131825904205311970493132056385139", loadedBookmark.cameraState().center().yPlainString());
        assertEquals("1000000000000.1234567890123456789", loadedBookmark.cameraState().zoomLevel().plainString());
    }

    @Test
    void loadsLegacyProjectDocumentWithoutMetadataAndSettings() throws IOException {
        Path projectPath = tempDir.resolve("legacy-project.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(projectPath.toFile(), new ProjectDocument(
                "legacy-id",
                "Legacy Project",
                "MANDELBROT",
                new RenderProfileDocument("legacy", 640, 360, 64, 4.0, "FINAL"),
                new ColorProfileDocument("legacy-colors", java.util.List.of(
                        new ColorStopDocument(0.0, 0.0, 0.0, 0.0),
                        new ColorStopDocument(1.0, 1.0, 1.0, 1.0)
                )),
                null,
                null,
                java.util.List.of(),
                null
        ));

        Project loadedProject = new ProjectFileRepository().load(projectPath);

        assertEquals("Legacy Project", loadedProject.name().value());
        assertEquals(6.0, loadedProject.settings().defaultFramesPerSecond());
        assertEquals(RenderPreset.STANDARD, loadedProject.settings().defaultRenderPreset());
    }

    @Test
    void loadsLegacyJsonWithNumericKeyframeCoordinates() throws IOException {
        Path projectPath = tempDir.resolve("legacy-keyframes.json");
        Files.writeString(projectPath, """
                {
                  "id": "legacy-id",
                  "name": "Legacy Project",
                  "fractalFormulaType": "MANDELBROT",
                  "renderProfile": {
                    "name": "legacy",
                    "width": 640,
                    "height": 360,
                    "maxIterations": 64,
                    "escapeRadius": 4.0,
                    "quality": "FINAL"
                  },
                  "colorProfile": {
                    "name": "legacy-colors",
                    "colorStops": [
                      { "position": 0.0, "red": 0.0, "green": 0.0, "blue": 0.0 },
                      { "position": 1.0, "red": 1.0, "green": 1.0, "blue": 1.0 }
                    ]
                  },
                  "keyframes": [
                    {
                      "id": "kf-1",
                      "seconds": 1.5,
                      "centerX": -0.7436438870371587,
                      "centerY": 0.13182590420531198,
                      "zoom": 2500000.0,
                      "label": "Legacy KF"
                    }
                  ]
                }
                """);

        Project loadedProject = new ProjectFileRepository().load(projectPath);
        Keyframe loadedKeyframe = loadedProject.timeline().keyframes().getFirst();

        assertEquals("Legacy KF", loadedKeyframe.label());
        assertEquals(-0.7436438870371587, loadedKeyframe.cameraState().center().x());
        assertEquals(0.13182590420531198, loadedKeyframe.cameraState().center().y());
        assertEquals(2500000.0, loadedKeyframe.cameraState().zoomLevel().value());
    }
}
