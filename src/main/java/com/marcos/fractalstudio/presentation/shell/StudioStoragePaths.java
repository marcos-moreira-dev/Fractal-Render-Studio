package com.marcos.fractalstudio.presentation.shell;

import java.nio.file.Path;

/**
 * Groups runtime storage paths used by the desktop shell.
 */
final class StudioStoragePaths {

    private final Path projectStorageDirectory;
    private final Path renderStorageDirectory;
    private final Path exportStorageDirectory;
    private final Path renderHistoryPath;
    private final Path autosaveProjectPath;

    private StudioStoragePaths(
            Path projectStorageDirectory,
            Path renderStorageDirectory,
            Path exportStorageDirectory,
            Path renderHistoryPath,
            Path autosaveProjectPath
    ) {
        this.projectStorageDirectory = projectStorageDirectory;
        this.renderStorageDirectory = renderStorageDirectory;
        this.exportStorageDirectory = exportStorageDirectory;
        this.renderHistoryPath = renderHistoryPath;
        this.autosaveProjectPath = autosaveProjectPath;
    }

    static StudioStoragePaths from(Path storageRoot) {
        Path projectStorageDirectory = storageRoot.resolve("projects");
        Path renderStorageDirectory = storageRoot.resolve("renders");
        Path exportStorageDirectory = storageRoot.resolve("exports");
        return new StudioStoragePaths(
                projectStorageDirectory,
                renderStorageDirectory,
                exportStorageDirectory,
                renderStorageDirectory.resolve("render-history.json"),
                projectStorageDirectory.resolve("latest.fractalstudio.json")
        );
    }

    Path projectStorageDirectory() {
        return projectStorageDirectory;
    }

    Path renderStorageDirectory() {
        return renderStorageDirectory;
    }

    Path exportStorageDirectory() {
        return exportStorageDirectory;
    }

    Path renderHistoryPath() {
        return renderHistoryPath;
    }

    Path autosaveProjectPath() {
        return autosaveProjectPath;
    }

    Path nextRenderOutputDirectory() {
        return renderStorageDirectory.resolve("render-" + System.currentTimeMillis());
    }
}
