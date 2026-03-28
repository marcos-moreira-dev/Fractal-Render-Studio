package com.marcos.fractalstudio;

import com.marcos.fractalstudio.presentation.app.ApplicationBootstrap;
import com.marcos.fractalstudio.presentation.app.StudioStageConfigurator;
import com.marcos.fractalstudio.presentation.app.StudioSplashScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(FractalStudioApplication.class);

    private final ApplicationBootstrap applicationBootstrap = new ApplicationBootstrap();
    private final StudioStageConfigurator stageConfigurator = new StudioStageConfigurator();

    @Override
    public void start(Stage stage) {
        LOGGER.info("Starting Fractal Render Studio");
        StudioSplashScreen splashScreen = new StudioSplashScreen();
        Platform.runLater(() -> {
            Parent root = applicationBootstrap.build();
            stageConfigurator.configure(stage, root, applicationBootstrap.appProperties());
            splashScreen.closeWhenReady(() -> {
                stageConfigurator.show(stage);
                LOGGER.info("Fractal Render Studio started successfully");
            });
        });
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Fractal Render Studio");
        applicationBootstrap.shutdown();
        LOGGER.info("Fractal Render Studio stopped");
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
