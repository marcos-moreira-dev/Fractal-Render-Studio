package com.marcos.fractalstudio.infrastructure.export;

import com.marcos.fractalstudio.application.export.ExportRequest;
import com.marcos.fractalstudio.application.export.ExportResult;
import com.marcos.fractalstudio.application.export.SequenceArchiveExporter;
import com.marcos.fractalstudio.infrastructure.exceptions.SequenceArchiveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writes a directory of rendered frames into a ZIP archive for optional manual export.
 */
public final class ZipSequenceArchiveExporter implements SequenceArchiveExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipSequenceArchiveExporter.class);

    @Override
    public ExportResult export(ExportRequest exportRequest) throws IOException {
        try {
            Files.createDirectories(exportRequest.destinationArchive().getParent());
            AtomicInteger exportedFiles = new AtomicInteger();
            List<Path> files = Files.walk(exportRequest.sourceDirectory())
                    .filter(Files::isRegularFile)
                    .toList();

            try (OutputStream outputStream = Files.newOutputStream(exportRequest.destinationArchive());
                 ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                for (Path path : files) {
                    writeEntry(exportRequest.sourceDirectory(), path, zipOutputStream, exportedFiles);
                }
            }

            LOGGER.info("Frame archive exported to {} with {} files",
                    exportRequest.destinationArchive().toAbsolutePath(),
                    exportedFiles.get());
            return new ExportResult(exportRequest.destinationArchive(), exportedFiles.get());
        } catch (IOException exception) {
            LOGGER.error("Failed to export frame archive to {}", exportRequest.destinationArchive().toAbsolutePath(), exception);
            throw new SequenceArchiveException("No se pudo generar el archivo ZIP de frames.", exception);
        }
    }

    private void writeEntry(Path sourceRoot, Path path, ZipOutputStream zipOutputStream, AtomicInteger exportedFiles) throws IOException {
        Path relativePath = sourceRoot.relativize(path);
        zipOutputStream.putNextEntry(new ZipEntry(relativePath.toString().replace('\\', '/')));
        Files.copy(path, zipOutputStream);
        zipOutputStream.closeEntry();
        exportedFiles.incrementAndGet();
    }
}
