package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.application.preview.PreviewCancellationToken;
import com.marcos.fractalstudio.application.preview.PreviewTileUpdate;
import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.color.RgbColor;
import com.marcos.fractalstudio.domain.fractal.FractalIterationMonitor;
import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;
import com.marcos.fractalstudio.domain.render.Resolution;

import java.math.BigDecimal;
import java.math.MathContext;

public abstract class AbstractFrameRenderer implements FrameRenderer {

    @Override
    public RenderedFrame render(FrameDescriptor frameDescriptor) {
        RenderComputation renderComputation = createRenderComputation(frameDescriptor);
        int[] pixels = new int[renderComputation.resolution().width() * renderComputation.resolution().height()];
        renderTile(
                renderComputation,
                pixels,
                0,
                0,
                renderComputation.resolution().width(),
                renderComputation.resolution().height(),
                new PreviewCancellationToken()
        );
        return new RenderedFrame(renderComputation.resolution().width(), renderComputation.resolution().height(), pixels);
    }

    protected final PreviewTileUpdate renderPreviewTile(
            FrameDescriptor frameDescriptor,
            int tileX,
            int tileY,
            int tileWidth,
            int tileHeight,
            int completedTiles,
            int totalTiles,
            PreviewCancellationToken cancellationToken
    ) {
        RenderComputation renderComputation = createRenderComputation(frameDescriptor);
        int[] pixels = new int[tileWidth * tileHeight];
        renderTile(renderComputation, pixels, tileX, tileY, tileWidth, tileHeight, cancellationToken);
        return new PreviewTileUpdate(
                renderComputation.resolution().width(),
                renderComputation.resolution().height(),
                tileX,
                tileY,
                tileWidth,
                tileHeight,
                pixels,
                completedTiles,
                totalTiles
        );
    }

    protected abstract EscapeParameters effectiveEscapeParameters(FrameDescriptor frameDescriptor);

    protected abstract int samplesPerAxis();

    protected abstract double colorCurve();

    protected boolean useHighPrecision(FrameDescriptor frameDescriptor) {
        return HighPrecisionQuadraticSampler.supports(frameDescriptor.fractalFormula())
                && HighPrecisionQuadraticSampler.shouldUseHighPrecision(frameDescriptor.cameraState().zoomLevel().valueDecimal());
    }

    private RenderComputation createRenderComputation(FrameDescriptor frameDescriptor) {
        Resolution resolution = frameDescriptor.renderProfile().resolution();
        CameraState cameraState = frameDescriptor.cameraState();
        BigDecimal zoom = cameraState.zoomLevel().valueDecimal();
        boolean highPrecision = useHighPrecision(frameDescriptor);
        MathContext mathContext = highPrecision
                ? HighPrecisionQuadraticSampler.mathContextForZoom(zoom)
                : MathContext.DECIMAL128;
        double aspectRatio = (double) resolution.width() / resolution.height();
        double zoomAsDouble = cameraState.zoomLevel().value();
        double planeHeight = 3.0 / zoomAsDouble;
        double planeWidth = planeHeight * aspectRatio;
        double minX = cameraState.center().x() - (planeWidth / 2.0);
        double minY = cameraState.center().y() - (planeHeight / 2.0);
        BigDecimal planeHeightDecimal = BigDecimal.valueOf(3L).divide(zoom, mathContext);
        BigDecimal planeWidthDecimal = planeHeightDecimal.multiply(BigDecimal.valueOf(aspectRatio), mathContext);
        BigDecimal minXDecimal = cameraState.center().xDecimal().subtract(
                planeWidthDecimal.divide(BigDecimal.valueOf(2L), mathContext),
                mathContext
        );
        BigDecimal minYDecimal = cameraState.center().yDecimal().subtract(
                planeHeightDecimal.divide(BigDecimal.valueOf(2L), mathContext),
                mathContext
        );

        return new RenderComputation(
                frameDescriptor,
                resolution,
                effectiveEscapeParameters(frameDescriptor),
                samplesPerAxis(),
                highPrecision,
                mathContext,
                minX,
                minY,
                planeWidth,
                planeHeight,
                minXDecimal,
                minYDecimal,
                planeWidthDecimal,
                planeHeightDecimal
        );
    }

    private void renderTile(
            RenderComputation renderComputation,
            int[] targetPixels,
            int tileX,
            int tileY,
            int tileWidth,
            int tileHeight,
            PreviewCancellationToken cancellationToken
    ) {
        for (int localPixelY = 0; localPixelY < tileHeight; localPixelY++) {
            cancellationToken.throwIfCancelled();
            int pixelY = tileY + localPixelY;
            for (int localPixelX = 0; localPixelX < tileWidth; localPixelX++) {
                cancellationToken.throwIfCancelled();
                int pixelX = tileX + localPixelX;
                RgbColor color = samplePixel(renderComputation, pixelX, pixelY, iterationMonitor(cancellationToken));
                targetPixels[(localPixelY * tileWidth) + localPixelX] = ArgbColorMapper.toArgb(color);
            }
        }
    }

    private FractalIterationMonitor iterationMonitor(PreviewCancellationToken cancellationToken) {
        return iteration -> {
            if ((iteration & 31) == 0) {
                cancellationToken.throwIfCancelled();
            }
        };
    }

    private RgbColor samplePixel(
            RenderComputation renderComputation,
            int pixelX,
            int pixelY,
            FractalIterationMonitor iterationMonitor
    ) {
        if (renderComputation.highPrecision()) {
            return samplePixelHighPrecision(renderComputation, pixelX, pixelY, iterationMonitor);
        }
        return samplePixelDouble(renderComputation, pixelX, pixelY, iterationMonitor);
    }

    private RgbColor samplePixelDouble(
            RenderComputation renderComputation,
            int pixelX,
            int pixelY,
            FractalIterationMonitor iterationMonitor
    ) {
        double totalRed = 0.0;
        double totalGreen = 0.0;
        double totalBlue = 0.0;
        int totalSamples = renderComputation.samplesPerAxis() * renderComputation.samplesPerAxis();

        for (int sampleY = 0; sampleY < renderComputation.samplesPerAxis(); sampleY++) {
            for (int sampleX = 0; sampleX < renderComputation.samplesPerAxis(); sampleX++) {
                double offsetX = (sampleX + 0.5) / renderComputation.samplesPerAxis();
                double offsetY = (sampleY + 0.5) / renderComputation.samplesPerAxis();
                double fractalX = renderComputation.minX()
                        + ((pixelX + offsetX) / renderComputation.resolution().width()) * renderComputation.planeWidth();
                double fractalY = renderComputation.minY()
                        + ((pixelY + offsetY) / renderComputation.resolution().height()) * renderComputation.planeHeight();
                double sample = renderComputation.frameDescriptor()
                        .fractalFormula()
                        .sample(fractalX, fractalY, renderComputation.escapeParameters(), iterationMonitor);
                RgbColor rgbColor = renderComputation.frameDescriptor()
                        .colorProfile()
                        .palette()
                        .sample(toneMappedSample(sample));
                totalRed += rgbColor.red();
                totalGreen += rgbColor.green();
                totalBlue += rgbColor.blue();
            }
        }

        return new RgbColor(totalRed / totalSamples, totalGreen / totalSamples, totalBlue / totalSamples);
    }

    private RgbColor samplePixelHighPrecision(
            RenderComputation renderComputation,
            int pixelX,
            int pixelY,
            FractalIterationMonitor iterationMonitor
    ) {
        double totalRed = 0.0;
        double totalGreen = 0.0;
        double totalBlue = 0.0;
        int totalSamples = renderComputation.samplesPerAxis() * renderComputation.samplesPerAxis();
        BigDecimal resolutionWidth = BigDecimal.valueOf(renderComputation.resolution().width());
        BigDecimal resolutionHeight = BigDecimal.valueOf(renderComputation.resolution().height());

        for (int sampleY = 0; sampleY < renderComputation.samplesPerAxis(); sampleY++) {
            for (int sampleX = 0; sampleX < renderComputation.samplesPerAxis(); sampleX++) {
                BigDecimal offsetX = BigDecimal.valueOf((sampleX + 0.5) / renderComputation.samplesPerAxis());
                BigDecimal offsetY = BigDecimal.valueOf((sampleY + 0.5) / renderComputation.samplesPerAxis());
                BigDecimal fractalX = renderComputation.minXDecimal().add(
                        BigDecimal.valueOf(pixelX).add(offsetX, renderComputation.mathContext())
                                .divide(resolutionWidth, renderComputation.mathContext())
                                .multiply(renderComputation.planeWidthDecimal(), renderComputation.mathContext()),
                        renderComputation.mathContext()
                );
                BigDecimal fractalY = renderComputation.minYDecimal().add(
                        BigDecimal.valueOf(pixelY).add(offsetY, renderComputation.mathContext())
                                .divide(resolutionHeight, renderComputation.mathContext())
                                .multiply(renderComputation.planeHeightDecimal(), renderComputation.mathContext()),
                        renderComputation.mathContext()
                );
                double sample = HighPrecisionMandelbrotSampler.sample(
                        renderComputation.frameDescriptor().fractalFormula(),
                        fractalX,
                        fractalY,
                        renderComputation.escapeParameters(),
                        renderComputation.mathContext(),
                        iterationMonitor
                );
                RgbColor rgbColor = renderComputation.frameDescriptor()
                        .colorProfile()
                        .palette()
                        .sample(toneMappedSample(sample));
                totalRed += rgbColor.red();
                totalGreen += rgbColor.green();
                totalBlue += rgbColor.blue();
            }
        }

        return new RgbColor(totalRed / totalSamples, totalGreen / totalSamples, totalBlue / totalSamples);
    }

    private double toneMappedSample(double sample) {
        if (sample <= 0.0) {
            return 0.0;
        }
        double curvedSample = Math.pow(sample, colorCurve());
        return Math.pow(curvedSample, 0.82);
    }

    private record RenderComputation(
            FrameDescriptor frameDescriptor,
            Resolution resolution,
            EscapeParameters escapeParameters,
            int samplesPerAxis,
            boolean highPrecision,
            MathContext mathContext,
            double minX,
            double minY,
            double planeWidth,
            double planeHeight,
            BigDecimal minXDecimal,
            BigDecimal minYDecimal,
            BigDecimal planeWidthDecimal,
            BigDecimal planeHeightDecimal
    ) {
    }
}
