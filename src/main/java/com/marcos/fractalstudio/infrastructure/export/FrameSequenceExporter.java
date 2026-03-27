package com.marcos.fractalstudio.infrastructure.export;

import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;

import java.io.IOException;
import java.nio.file.Path;

public interface FrameSequenceExporter {

    void exportFrame(Path outputDirectory, FrameDescriptor frameDescriptor, RenderedFrame renderedFrame) throws IOException;
}
