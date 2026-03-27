package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;

public final class FinalFrameRenderer extends AbstractFrameRenderer {

    @Override
    protected EscapeParameters effectiveEscapeParameters(FrameDescriptor frameDescriptor) {
        return AdaptiveEscapeBudget.forFinal(frameDescriptor);
    }

    @Override
    protected int samplesPerAxis() {
        return 2;
    }

    @Override
    protected double colorCurve() {
        return 0.72;
    }
}
