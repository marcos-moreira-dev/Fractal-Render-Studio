package com.marcos.fractalstudio.infrastructure.export;

import com.marcos.fractalstudio.application.export.ExportRequest;
import com.marcos.fractalstudio.application.export.ExportResult;
import com.marcos.fractalstudio.application.export.SequenceArchiveExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipSequenceArchiveExporter implements SequenceArchiveExporter {

    @Override
    public ExportResult export(ExportRequest exportRequest) throws IOException {
        Files.createDirectories(exportRequest.destinationArchive().getParent());
        AtomicInteger exportedFiles = new AtomicInteger();

        try (OutputStream outputStream = Files.newOutputStream(exportRequest.destinationArchive());
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            Files.walk(exportRequest.sourceDirectory())
                    .filter(Files::isRegularFile)
                    .forEach(path -> writeEntry(exportRequest.sourceDirectory(), path, zipOutputStream, exportedFiles));
        }

        return new ExportResult(exportRequest.destinationArchive(), exportedFiles.get());
    }

    private void writeEntry(Path sourceRoot, Path path, ZipOutputStream zipOutputStream, AtomicInteger exportedFiles) {
        Path relativePath = sourceRoot.relativize(path);
        try {
            zipOutputStream.putNextEntry(new ZipEntry(relativePath.toString().replace('\\', '/')));
            Files.copy(path, zipOutputStream);
            zipOutputStream.closeEntry();
            exportedFiles.incrementAndGet();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
