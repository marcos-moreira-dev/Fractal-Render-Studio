package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.application.project.CreateProjectUseCase;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.infrastructure.rendering.TimelineThumbnailRenderer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GenerateKeyframeThumbnailUseCaseTest {

    @Test
    void generatesLowCostThumbnailFrame() {
        Project project = new CreateProjectUseCase().create("Thumbnail Test");
        CameraState cameraState = new CameraState(new FractalCoordinate(-0.5, 0.0), new ZoomLevel(1.0));
        GenerateKeyframeThumbnailUseCase useCase = new GenerateKeyframeThumbnailUseCase(
                new TimelineThumbnailRenderer(),
                Runnable::run
        );

        RenderedFrame thumbnail = useCase.generate(project, cameraState).join();

        assertEquals(160, thumbnail.width());
        assertEquals(90, thumbnail.height());
    }
}
