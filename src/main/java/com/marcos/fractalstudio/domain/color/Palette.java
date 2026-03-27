package com.marcos.fractalstudio.domain.color;

import java.util.Comparator;
import java.util.List;

public record Palette(List<ColorStop> colorStops) {

    public Palette {
        if (colorStops == null || colorStops.isEmpty()) {
            throw new IllegalArgumentException("A palette requires at least one color stop.");
        }
        colorStops = colorStops.stream()
                .sorted(Comparator.comparingDouble(ColorStop::position))
                .toList();
    }

    public RgbColor sample(double value) {
        double clampedValue = Math.max(0.0, Math.min(1.0, value));
        if (clampedValue <= colorStops.getFirst().position()) {
            return colorStops.getFirst().color();
        }
        if (clampedValue >= colorStops.getLast().position()) {
            return colorStops.getLast().color();
        }

        for (int index = 0; index < colorStops.size() - 1; index++) {
            ColorStop start = colorStops.get(index);
            ColorStop end = colorStops.get(index + 1);
            if (clampedValue >= start.position() && clampedValue <= end.position()) {
                double range = end.position() - start.position();
                double ratio = range == 0.0 ? 0.0 : (clampedValue - start.position()) / range;
                return RgbColor.interpolate(start.color(), end.color(), ratio);
            }
        }

        return colorStops.getLast().color();
    }
}
