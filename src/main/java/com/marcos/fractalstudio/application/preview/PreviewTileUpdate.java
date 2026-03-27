package com.marcos.fractalstudio.application.preview;

public record PreviewTileUpdate(
        int frameWidth,
        int frameHeight,
        int tileX,
        int tileY,
        int tileWidth,
        int tileHeight,
        int[] argbPixels,
        int completedTiles,
        int totalTiles
) {

    public PreviewTileUpdate {
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalArgumentException("Preview frame dimensions must be positive.");
        }
        if (tileX < 0 || tileY < 0 || tileWidth <= 0 || tileHeight <= 0) {
            throw new IllegalArgumentException("Preview tile bounds are invalid.");
        }
        if (tileX + tileWidth > frameWidth || tileY + tileHeight > frameHeight) {
            throw new IllegalArgumentException("Preview tile exceeds frame bounds.");
        }
        if (argbPixels == null || argbPixels.length != tileWidth * tileHeight) {
            throw new IllegalArgumentException("Preview tile pixel buffer size is invalid.");
        }
        if (completedTiles < 0 || totalTiles <= 0 || completedTiles > totalTiles) {
            throw new IllegalArgumentException("Preview tile completion counters are invalid.");
        }
    }

    public int progressPercentage() {
        return (int) Math.round((completedTiles * 100.0) / totalTiles);
    }
}
