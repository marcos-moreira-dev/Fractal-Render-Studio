package com.marcos.fractalstudio.domain.camera;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PrecisionTypesTest {

    @Test
    void preservesCoordinatePrecisionAsPlainStrings() {
        FractalCoordinate coordinate = new FractalCoordinate(
                new BigDecimal("-0.743643887037158704752191506114774"),
                new BigDecimal("0.131825904205311970493132056385139")
        );

        assertEquals("-0.743643887037158704752191506114774", coordinate.xPlainString());
        assertEquals("0.131825904205311970493132056385139", coordinate.yPlainString());
    }

    @Test
    void preservesZoomPrecisionAsPlainString() {
        ZoomLevel zoomLevel = new ZoomLevel(new BigDecimal("1000000000000.1234567890123456789"));

        assertEquals("1000000000000.1234567890123456789", zoomLevel.plainString());
    }
}
