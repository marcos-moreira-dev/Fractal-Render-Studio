package com.marcos.fractalstudio.infrastructure.rendering;

import com.marcos.fractalstudio.domain.color.RgbColor;

public final class ArgbColorMapper {

    private ArgbColorMapper() {
    }

    public static int toArgb(RgbColor rgbColor) {
        int red = (int) Math.round(rgbColor.red() * 255.0);
        int green = (int) Math.round(rgbColor.green() * 255.0);
        int blue = (int) Math.round(rgbColor.blue() * 255.0);
        return (255 << 24) | (red << 16) | (green << 8) | blue;
    }
}
