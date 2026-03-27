package com.marcos.fractalstudio.infrastructure.export;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

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

    @Override
    public Path exportVideo(Path sourceDirectory, Path destinationVideo, double framesPerSecond) throws IOException {
        Files.createDirectories(destinationVideo.getParent());
        List<Path> frameFiles = Files.list(sourceDirectory)
                .filter(path -> path.getFileName().toString().startsWith("frame_"))
                .filter(path -> path.getFileName().toString().endsWith(".png"))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .toList();
        if (frameFiles.isEmpty()) {
            throw new IOException("No hay frames PNG para codificar video.");
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
        } catch (org.bytedeco.javacv.FFmpegFrameRecorder.Exception exception) {
            throw new IOException("No se pudo codificar el video MP4.", exception);
        } finally {
            try {
                recorder.release();
            } catch (org.bytedeco.javacv.FFmpegFrameRecorder.Exception ignored) {
                // Release is best-effort after stop failure.
            }
            converter.close();
        }
        return destinationVideo;
    }

    private BufferedImage readFrame(Path frameFile) throws IOException {
        BufferedImage image = ImageIO.read(frameFile.toFile());
        if (image == null) {
            throw new IOException("No se pudo leer el frame " + frameFile.getFileName());
        }
        return image;
    }

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

    private int ensureEven(int value) {
        return value % 2 == 0 ? value : value + 1;
    }
}
