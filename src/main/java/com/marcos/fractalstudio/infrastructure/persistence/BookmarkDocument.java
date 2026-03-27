package com.marcos.fractalstudio.infrastructure.persistence;

public record BookmarkDocument(
        String id,
        String label,
        String centerX,
        String centerY,
        String zoom
) {
}
