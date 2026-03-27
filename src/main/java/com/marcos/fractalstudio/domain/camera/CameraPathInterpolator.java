package com.marcos.fractalstudio.domain.camera;

import java.math.BigDecimal;
import java.math.MathContext;

public final class CameraPathInterpolator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private CameraPathInterpolator() {
    }

    public static CameraState interpolate(CameraState start, CameraState end, double ratio) {
        double clampedRatio = Math.max(0.0, Math.min(1.0, ratio));
        BigDecimal ratioDecimal = BigDecimal.valueOf(clampedRatio);
        BigDecimal interpolatedX = interpolateLinear(start.center().xDecimal(), end.center().xDecimal(), ratioDecimal);
        BigDecimal interpolatedY = interpolateLinear(start.center().yDecimal(), end.center().yDecimal(), ratioDecimal);
        double startZoom = start.zoomLevel().value();
        double endZoom = end.zoomLevel().value();
        double interpolatedZoom = startZoom * Math.pow(endZoom / startZoom, clampedRatio);

        return new CameraState(
                new FractalCoordinate(interpolatedX, interpolatedY),
                new ZoomLevel(interpolatedZoom)
        );
    }

    private static BigDecimal interpolateLinear(BigDecimal start, BigDecimal end, BigDecimal ratio) {
        return start.add(
                end.subtract(start, MATH_CONTEXT).multiply(ratio, MATH_CONTEXT),
                MATH_CONTEXT
        );
    }
}
