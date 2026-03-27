package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.domain.render.RenderQuality;

public final class FrameRendererFactory {

    private final FrameRenderer previewFrameRenderer;
    private final FrameRenderer finalFrameRenderer;

    public FrameRendererFactory(FrameRenderer previewFrameRenderer, FrameRenderer finalFrameRenderer) {
        this.previewFrameRenderer = previewFrameRenderer;
        this.finalFrameRenderer = finalFrameRenderer;
    }

    public FrameRenderer create(RenderQuality renderQuality) {
        return switch (renderQuality) {
            case PREVIEW -> previewFrameRenderer;
            case FINAL -> finalFrameRenderer;
        };
    }
}
