package com.marcos.fractalstudio.infrastructure.export;

import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PngFrameSequenceExporter implements FrameSequenceExporter {

    @Override
    public void exportFrame(Path outputDirectory, FrameDescriptor frameDescriptor, RenderedFrame renderedFrame) throws IOException {
        Files.createDirectories(outputDirectory);
        BufferedImage bufferedImage = new BufferedImage(renderedFrame.width(), renderedFrame.height(), BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, renderedFrame.width(), renderedFrame.height(), renderedFrame.argbPixels(), 0, renderedFrame.width());
        String filename = "frame_%05d.png".formatted(frameDescriptor.frameIndex().value());
        ImageIO.write(bufferedImage, "png", outputDirectory.resolve(filename).toFile());
    }
}
