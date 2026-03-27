package com.marcos.fractalstudio.infrastructure.export;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Encodes a rendered frame directory into a playable video file.
 */
public interface SequenceVideoExporter {

    Path exportVideo(Path sourceDirectory, Path destinationVideo, double framesPerSecond) throws IOException;
}
