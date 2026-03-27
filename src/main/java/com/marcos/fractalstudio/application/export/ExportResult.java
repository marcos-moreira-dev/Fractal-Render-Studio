package com.marcos.fractalstudio.application.export;

import java.nio.file.Path;

public record ExportResult(Path archivePath, int exportedFileCount) {
}
