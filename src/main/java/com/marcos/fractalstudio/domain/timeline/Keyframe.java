package com.marcos.fractalstudio.domain.timeline;

import com.marcos.fractalstudio.domain.camera.CameraState;

public record Keyframe(KeyframeId id, TimePosition timePosition, CameraState cameraState, String label) {

    public Keyframe {
        if (id == null || timePosition == null || cameraState == null) {
            throw new IllegalArgumentException("Keyframe requires id, time position, and camera state.");
        }
    }
}
