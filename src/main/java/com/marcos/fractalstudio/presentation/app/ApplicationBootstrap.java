package com.marcos.fractalstudio.presentation.app;

import com.marcos.fractalstudio.presentation.common.UiThreadExecutor;
import com.marcos.fractalstudio.presentation.shell.StudioShellViewModel;

import javafx.scene.Parent;

import java.nio.file.Path;

/**
 * Assembles application services, infrastructure adapters and the UI composition root.
 */
public final class ApplicationBootstrap {

    private final AppProperties appProperties = new AppPropertiesLoader().load();
    private final StudioRuntimeExecutors runtimeExecutors = new StudioRuntimeExecutors(appProperties);
    private StudioShellViewModel studioShellViewModel;

    /**
     * Builds the root JavaFX node and the service graph backing it.
     *
     * @return application root node
     */
    public Parent build() {
        UiThreadExecutor uiThreadExecutor = new UiThreadExecutor();
        InfrastructureServices infrastructure = new InfrastructureServicesFactory().create(runtimeExecutors);
        ApplicationServices applicationServices = new ApplicationServicesFactory().create(
                infrastructure,
                runtimeExecutors
        );

        studioShellViewModel = new StudioShellViewModel(
                applicationServices.projectFacade(),
                applicationServices.renderFacade(),
                applicationServices.exportFacade(),
                uiThreadExecutor,
                Path.of(appProperties.storageRoot())
        );

        return new UiCompositionRoot().compose(studioShellViewModel);
    }

    /**
     * Returns the effective runtime settings.
     *
     * @return runtime properties loaded from configuration
     */
    public AppProperties appProperties() {
        return appProperties;
    }

    /**
     * Stops background executors used by preview and final rendering.
     */
    public void shutdown() {
        if (studioShellViewModel != null) {
            studioShellViewModel.shutdownSession();
        }
        runtimeExecutors.shutdown();
    }
}
