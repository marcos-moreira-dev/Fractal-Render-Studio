package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.application.preview.PreviewCancellationToken;
import com.marcos.fractalstudio.application.preview.PreviewTileUpdate;
import com.marcos.fractalstudio.application.preview.TiledPreviewRenderer;
import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;

public final class PreviewFrameRenderer extends AbstractFrameRenderer implements TiledPreviewRenderer {

    @Override
    protected EscapeParameters effectiveEscapeParameters(FrameDescriptor frameDescriptor) {
        return AdaptiveEscapeBudget.forPreview(frameDescriptor);
    }

    @Override
    protected int samplesPerAxis() {
        return 1;
    }

    @Override
    protected double colorCurve() {
        return 0.85;
    }

    @Override
    protected boolean useHighPrecision(FrameDescriptor frameDescriptor) {
        if (frameDescriptor.renderProfile().name().contains("Fast")
                && !frameDescriptor.renderProfile().name().contains("High Precision")) {
            return false;
        }
        return super.useHighPrecision(frameDescriptor);
    }

    @Override
    public PreviewTileUpdate renderTile(FrameDescriptor frameDescriptor, int tileX, int tileY, int tileWidth, int tileHeight) {
        return renderTile(frameDescriptor, tileX, tileY, tileWidth, tileHeight, new PreviewCancellationToken());
    }

    @Override
    public PreviewTileUpdate renderTile(
            FrameDescriptor frameDescriptor,
            int tileX,
            int tileY,
            int tileWidth,
            int tileHeight,
            PreviewCancellationToken cancellationToken
    ) {
        return renderPreviewTile(frameDescriptor, tileX, tileY, tileWidth, tileHeight, 1, 1, cancellationToken);
    }
}
