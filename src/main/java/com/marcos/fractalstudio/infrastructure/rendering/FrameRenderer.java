package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;

public interface FrameRenderer {

    RenderedFrame render(FrameDescriptor frameDescriptor);
}
