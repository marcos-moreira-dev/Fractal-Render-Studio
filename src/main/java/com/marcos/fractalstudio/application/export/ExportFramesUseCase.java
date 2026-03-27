package com.marcos.fractalstudio.application.export;

import java.io.IOException;

public final class ExportFramesUseCase {

    private final SequenceArchiveExporter sequenceArchiveExporter;

    public ExportFramesUseCase(SequenceArchiveExporter sequenceArchiveExporter) {
        this.sequenceArchiveExporter = sequenceArchiveExporter;
    }

    public ExportResult export(ExportRequest exportRequest) throws IOException {
        return sequenceArchiveExporter.export(exportRequest);
    }
}
