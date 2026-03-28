package com.marcos.fractalstudio.infrastructure.export;

import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;
import com.marcos.fractalstudio.infrastructure.exceptions.FrameExportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PngFrameSequenceExporter implements FrameSequenceExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PngFrameSequenceExporter.class);

    @Override
    /**
     * Writes a single rendered frame as a PNG file inside the render workspace.
     *
     * <p>Frame export is treated as an infrastructure concern because it depends on filesystem access
     * and image encoders rather than on the mathematical meaning of the render itself.
     */
    public void exportFrame(Path outputDirectory, FrameDescriptor frameDescriptor, RenderedFrame renderedFrame) throws IOException {
        try {
            Files.createDirectories(outputDirectory);
            BufferedImage bufferedImage = new BufferedImage(renderedFrame.width(), renderedFrame.height(), BufferedImage.TYPE_INT_ARGB);
            bufferedImage.setRGB(0, 0, renderedFrame.width(), renderedFrame.height(), renderedFrame.argbPixels(), 0, renderedFrame.width());
            String filename = "frame_%05d.png".formatted(frameDescriptor.frameIndex().value());
            boolean written = ImageIO.write(bufferedImage, "png", outputDirectory.resolve(filename).toFile());
            if (!written) {
                throw new FrameExportException("No se encontró un codificador PNG disponible.", null);
            }
        } catch (IOException exception) {
            LOGGER.error("Failed to export frame {} to {}", frameDescriptor.frameIndex().value(), outputDirectory.toAbsolutePath(), exception);
            throw new FrameExportException("No se pudo exportar un frame PNG.", exception);
        }
    }
}
