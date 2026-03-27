package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.domain.render.RenderQuality;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

final class FrameRendererFactoryTest {

    @Test
    void resolvesRendererByQuality() {
        FrameRenderer previewRenderer = frameDescriptor -> new RenderedFrame(1, 1, new int[]{0});
        FrameRenderer finalRenderer = frameDescriptor -> new RenderedFrame(1, 1, new int[]{0});
        FrameRendererFactory factory = new FrameRendererFactory(previewRenderer, finalRenderer);

        assertSame(previewRenderer, factory.create(RenderQuality.PREVIEW));
        assertSame(finalRenderer, factory.create(RenderQuality.FINAL));
    }
}
