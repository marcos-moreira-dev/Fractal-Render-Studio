package com.marcos.fractalstudio.domain.render;

import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.color.ColorProfile;
import com.marcos.fractalstudio.domain.fractal.FractalFormula;
import com.marcos.fractalstudio.domain.timeline.TimePosition;

public record FrameDescriptor(
        FrameIndex frameIndex,
        TimePosition timePosition,
        CameraState cameraState,
        FractalFormula fractalFormula,
        RenderProfile renderProfile,
        ColorProfile colorProfile
) {

    public FrameDescriptor {
        if (frameIndex == null || timePosition == null || cameraState == null || fractalFormula == null
                || renderProfile == null || colorProfile == null) {
            throw new IllegalArgumentException("Frame descriptor requires all fields.");
        }
    }
}
