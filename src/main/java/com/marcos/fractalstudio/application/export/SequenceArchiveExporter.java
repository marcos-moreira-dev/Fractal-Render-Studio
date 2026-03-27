package com.marcos.fractalstudio.application.export;

import java.io.IOException;

public interface SequenceArchiveExporter {

    ExportResult export(ExportRequest exportRequest) throws IOException;
}
