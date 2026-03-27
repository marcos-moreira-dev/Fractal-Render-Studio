package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;

public final class TimelineThumbnailRenderer extends AbstractFrameRenderer {

    @Override
    protected EscapeParameters effectiveEscapeParameters(FrameDescriptor frameDescriptor) {
        return frameDescriptor.renderProfile().escapeParameters();
    }

    @Override
    protected int samplesPerAxis() {
        return 1;
    }

    @Override
    protected double colorCurve() {
        return 0.85;
    }

    @Override
    protected boolean useHighPrecision(FrameDescriptor frameDescriptor) {
        return false;
    }
}
