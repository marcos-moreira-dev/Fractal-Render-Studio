package com.marcos.fractalstudio.application.export;

import java.nio.file.Path;

public record ExportRequest(Path sourceDirectory, Path destinationArchive) {

    public ExportRequest {
        if (sourceDirectory == null || destinationArchive == null) {
            throw new IllegalArgumentException("Export request requires source and destination paths.");
        }
    }
}
