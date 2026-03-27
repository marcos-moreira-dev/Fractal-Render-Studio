package com.marcos.fractalstudio.infrastructure.export;

import com.marcos.fractalstudio.application.export.ExportRequest;
import com.marcos.fractalstudio.application.export.ExportResult;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ZipSequenceArchiveExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void archivesRenderedSequenceDirectory() throws IOException {
        Path sourceDirectory = tempDir.resolve("frames");
        Files.createDirectories(sourceDirectory);
        Files.writeString(sourceDirectory.resolve("frame_00001.png"), "frame-a");
        Files.writeString(sourceDirectory.resolve("frame_00002.png"), "frame-b");

        Path archivePath = tempDir.resolve("exports").resolve("frames.zip");
        ExportResult exportResult = new ZipSequenceArchiveExporter().export(new ExportRequest(sourceDirectory, archivePath));

        assertTrue(Files.exists(archivePath));
        assertEquals(2, exportResult.exportedFileCount());
    }
}
