package com.marcos.fractalstudio.domain.color;

public record RgbColor(double red, double green, double blue) {

    public RgbColor {
        validateChannel(red, "red");
        validateChannel(green, "green");
        validateChannel(blue, "blue");
    }

    private static void validateChannel(double value, String name) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " channel must be between 0 and 1.");
        }
    }

    public static RgbColor interpolate(RgbColor start, RgbColor end, double ratio) {
        double clampedRatio = Math.max(0.0, Math.min(1.0, ratio));
        return new RgbColor(
                start.red + ((end.red - start.red) * clampedRatio),
                start.green + ((end.green - start.green) * clampedRatio),
                start.blue + ((end.blue - start.blue) * clampedRatio)
        );
    }
}
