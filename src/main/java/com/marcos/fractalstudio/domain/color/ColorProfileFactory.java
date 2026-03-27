package com.marcos.fractalstudio.domain.color;

import java.util.List;

public final class ColorProfileFactory {

    private ColorProfileFactory() {
    }

    public static ColorProfile createDefault() {
        return create("Sunset Escape");
    }

    public static List<String> availableProfileNames() {
        return List.of("Sunset Escape", "Electric Ice", "Monochrome Ember");
    }

    public static ColorProfile create(String profileName) {
        return switch (profileName) {
            case "Sunset Escape" -> new ColorProfile("Sunset Escape", new Palette(List.of(
                    new ColorStop(0.0, new RgbColor(0.03, 0.04, 0.10)),
                    new ColorStop(0.35, new RgbColor(0.15, 0.28, 0.62)),
                    new ColorStop(0.65, new RgbColor(0.97, 0.66, 0.20)),
                    new ColorStop(1.0, new RgbColor(1.0, 0.97, 0.84))
            )));
            case "Electric Ice" -> new ColorProfile("Electric Ice", new Palette(List.of(
                    new ColorStop(0.0, new RgbColor(0.02, 0.03, 0.09)),
                    new ColorStop(0.28, new RgbColor(0.12, 0.42, 0.82)),
                    new ColorStop(0.62, new RgbColor(0.40, 0.92, 0.98)),
                    new ColorStop(1.0, new RgbColor(0.95, 1.0, 1.0))
            )));
            case "Monochrome Ember" -> new ColorProfile("Monochrome Ember", new Palette(List.of(
                    new ColorStop(0.0, new RgbColor(0.02, 0.02, 0.03)),
                    new ColorStop(0.45, new RgbColor(0.30, 0.30, 0.35)),
                    new ColorStop(0.78, new RgbColor(0.86, 0.50, 0.18)),
                    new ColorStop(1.0, new RgbColor(0.99, 0.96, 0.90))
            )));
            default -> throw new IllegalArgumentException("Unsupported color profile: " + profileName);
        };
    }
}
