package com.marcos.fractalstudio.domain.timeline;

import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TimelineTest {

    @Test
    void resolvesInterpolatedCameraBetweenTwoKeyframes() {
        Timeline timeline = new Timeline()
                .addKeyframe(new Keyframe(
                KeyframeId.create(),
                new TimePosition(0.0),
                new CameraState(new FractalCoordinate(-1.0, 0.0), new ZoomLevel(1.0)),
                "start"
        ))
                .addKeyframe(new Keyframe(
                KeyframeId.create(),
                new TimePosition(10.0),
                new CameraState(new FractalCoordinate(1.0, 2.0), new ZoomLevel(4.0)),
                "end"
        ));

        CameraState resolved = timeline.resolveCameraAt(
                new TimePosition(5.0),
                new CameraState(new FractalCoordinate(0.0, 0.0), new ZoomLevel(1.0))
        );

        assertEquals(0.0, resolved.center().x(), 0.0001);
        assertEquals(1.0, resolved.center().y(), 0.0001);
        assertEquals(2.0, resolved.zoomLevel().value(), 0.0001);
    }
}
