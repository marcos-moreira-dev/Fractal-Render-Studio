package com.marcos.fractalstudio;

import com.marcos.fractalstudio.presentation.app.ApplicationBootstrap;
import com.marcos.fractalstudio.presentation.app.StudioStageConfigurator;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Main JavaFX entry point for Fractal Render Studio.
 *
 * <p>This class intentionally lives at the root package because it is the public starting point of the
 * desktop application. Its responsibility is deliberately small: bootstrap the application graph,
 * delegate window configuration and keep shutdown centralized. The goal is to make the repository easier
 * to read from the outside without exposing presentation package details as the canonical entry point.
 */
public final class FractalStudioApplication extends Application {

    private final ApplicationBootstrap applicationBootstrap = new ApplicationBootstrap();
    private final StudioStageConfigurator stageConfigurator = new StudioStageConfigurator();

    @Override
    public void start(Stage stage) {
        Parent root = applicationBootstrap.build();
        stageConfigurator.configureAndShow(stage, root, applicationBootstrap.appProperties());
    }

    @Override
    public void stop() {
        applicationBootstrap.shutdown();
    }

    /**
     * Launches the JavaFX desktop application.
     *
     * @param args startup arguments forwarded by the JVM
     */
    public static void main(String[] args) {
        launch(args);
    }
}
