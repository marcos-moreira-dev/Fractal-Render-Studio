package com.marcos.fractalstudio.infrastructure.persistence;

import java.util.List;

public record ColorProfileDocument(String name, List<ColorStopDocument> colorStops) {
}
