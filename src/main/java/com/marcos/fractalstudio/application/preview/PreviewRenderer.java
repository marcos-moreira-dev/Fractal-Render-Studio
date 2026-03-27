package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.domain.render.FrameDescriptor;

public interface PreviewRenderer {

    RenderedFrame render(FrameDescriptor frameDescriptor);
}
