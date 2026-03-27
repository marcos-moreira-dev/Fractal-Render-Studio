package com.marcos.fractalstudio.domain.camera;

public record CameraState(FractalCoordinate center, ZoomLevel zoomLevel) {

    public CameraState {
        if (center == null) {
            throw new IllegalArgumentException("Camera center is required.");
        }
        if (zoomLevel == null) {
            throw new IllegalArgumentException("Zoom level is required.");
        }
    }

    public CameraState pan(double deltaX, double deltaY) {
        return new CameraState(center.add(deltaX, deltaY), zoomLevel);
    }

    public CameraState zoomBy(double factor) {
        return new CameraState(center, zoomLevel.multiply(factor));
    }
}
