package com.marcos.fractalstudio.infrastructure.export;

import com.marcos.fractalstudio.infrastructure.exceptions.VideoEncodingException;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Produces Windows-compatible MP4 files using FFmpeg through JavaCV.
 */
public final class Mp4SequenceVideoExporter implements SequenceVideoExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mp4SequenceVideoExporter.class);

    @Override
    /**
     * Encodes a directory of sequential PNG frames into a Windows-compatible
     * MP4 file.
     *
     * <p>The exporter expects the source directory to contain files named like
     * {@code frame_000001.png}. It preserves frame ordering lexicographically,
     * normalizes odd dimensions to even values required by common H.264
     * pipelines and writes a single MP4 artifact suitable for the desktop
     * workflow of the application.
     *
     * @param sourceDirectory directory containing temporary PNG frames
     * @param destinationVideo target MP4 file to create or replace
     * @param framesPerSecond playback frame rate for the output video
     * @return the written video path for chaining by callers
     * @throws IOException when no frames exist or FFmpeg encoding fails
     */
    public Path exportVideo(Path sourceDirectory, Path destinationVideo, double framesPerSecond) throws IOException {
        try {
            Files.createDirectories(destinationVideo.getParent());
            List<Path> frameFiles;
            try (var frameStream = Files.list(sourceDirectory)) {
                frameFiles = frameStream
                        .filter(path -> path.getFileName().toString().startsWith("frame_"))
                        .filter(path -> path.getFileName().toString().endsWith(".png"))
                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .toList();
            }
            if (frameFiles.isEmpty()) {
                throw new VideoEncodingException("No hay frames PNG para codificar video.", null);
            }

            BufferedImage firstImage = readFrame(frameFiles.getFirst());
            int width = ensureEven(firstImage.getWidth());
            int height = ensureEven(firstImage.getHeight());

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(destinationVideo.toFile(), width, height);
            Java2DFrameConverter converter = new Java2DFrameConverter();
            try {
                recorder.setFormat("mp4");
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setFrameRate(framesPerSecond);
                recorder.setVideoBitrate(Math.max(4_000_000, width * height * 4));
                recorder.setGopSize(Math.max(12, (int) Math.round(framesPerSecond)));
                recorder.start();

                for (Path frameFile : frameFiles) {
                    BufferedImage image = normalizeFrame(readFrame(frameFile), width, height);
                    Frame frame = converter.convert(image);
                    recorder.record(frame);
                }
                recorder.stop();
            } finally {
                try {
                    recorder.release();
                } catch (org.bytedeco.javacv.FFmpegFrameRecorder.Exception ignored) {
                    // Best-effort resource release after stop/start failure.
                }
                converter.close();
            }
            LOGGER.info("MP4 encoded to {} from {} frames at {} fps",
                    destinationVideo.toAbsolutePath(),
                    frameFiles.size(),
                    framesPerSecond);
        } catch (org.bytedeco.javacv.FFmpegFrameRecorder.Exception exception) {
            LOGGER.error("FFmpeg failed while encoding {}", destinationVideo.toAbsolutePath(), exception);
            throw new VideoEncodingException("No se pudo codificar el video MP4.", exception);
        } catch (IOException exception) {
            LOGGER.error("Video export failed for destination {}", destinationVideo.toAbsolutePath(), exception);
            throw new VideoEncodingException("No se pudo preparar el video MP4.", exception);
        }
        return destinationVideo;
    }

    /**
     * Loads a PNG frame from disk and fails fast when the image cannot be
     * decoded, keeping the render pipeline explicit about corrupt or missing
     * intermediate artifacts.
     */
    private BufferedImage readFrame(Path frameFile) throws IOException {
        BufferedImage image = ImageIO.read(frameFile.toFile());
        if (image == null) {
            throw new IOException("No se pudo leer el frame " + frameFile.getFileName());
        }
        return image;
    }

    /**
     * Converts source frames to a consistent even-sized BGR image expected by
     * the recorder, resizing only when necessary.
     */
    private BufferedImage normalizeFrame(BufferedImage source, int width, int height) {
        if (source.getWidth() == width && source.getHeight() == height && source.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return source;
        }
        BufferedImage converted = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = converted.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return converted;
    }

    /**
     * H.264 pipelines often require even dimensions because of chroma
     * subsampling. The exporter therefore rounds odd values up to the next even
     * number before initializing the recorder.
     */
    private int ensureEven(int value) {
        return value % 2 == 0 ? value : value + 1;
    }
}
