package com.marcos.fractalstudio.application.preview;

import com.marcos.fractalstudio.domain.render.FrameDescriptor;

public interface TiledPreviewRenderer extends PreviewRenderer {

    PreviewTileUpdate renderTile(FrameDescriptor frameDescriptor, int tileX, int tileY, int tileWidth, int tileHeight);

    default PreviewTileUpdate renderTile(
            FrameDescriptor frameDescriptor,
            int tileX,
            int tileY,
            int tileWidth,
            int tileHeight,
            PreviewCancellationToken cancellationToken
    ) {
        cancellationToken.throwIfCancelled();
        return renderTile(frameDescriptor, tileX, tileY, tileWidth, tileHeight);
    }

    default PreviewTileUpdate renderTile(
            FrameDescriptor frameDescriptor,
            int tileX,
            int tileY,
            int tileWidth,
            int tileHeight,
            int completedTiles,
            int totalTiles,
            PreviewCancellationToken cancellationToken
    ) {
        PreviewTileUpdate tileUpdate = renderTile(frameDescriptor, tileX, tileY, tileWidth, tileHeight, cancellationToken);
        return new PreviewTileUpdate(
                tileUpdate.frameWidth(),
                tileUpdate.frameHeight(),
                tileUpdate.tileX(),
                tileUpdate.tileY(),
                tileUpdate.tileWidth(),
                tileUpdate.tileHeight(),
                tileUpdate.argbPixels(),
                completedTiles,
                totalTiles
        );
    }
}
