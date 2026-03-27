package com.marcos.fractalstudio.presentation.app;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the desktop studio.
 */
public final class FractalStudioApplication extends Application {

    private final ApplicationBootstrap applicationBootstrap = new ApplicationBootstrap();

    @Override
    public void start(Stage stage) {
        Parent root = applicationBootstrap.build();
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double initialWidth = Math.min(applicationBootstrap.appProperties().initialWidth(), visualBounds.getWidth() * 0.92);
        double initialHeight = Math.min(applicationBootstrap.appProperties().initialHeight(), visualBounds.getHeight() * 0.90);
        Scene scene = new Scene(root, initialWidth, initialHeight);
        scene.getStylesheets().add(getClass().getResource("/assets/styles/studio.css").toExternalForm());

        stage.setTitle(applicationBootstrap.appProperties().applicationTitle());
        stage.getIcons().setAll(new Image(getClass().getResourceAsStream("/assets/icons/app-icon.png")));
        stage.setMinWidth(Math.min(1080.0, visualBounds.getWidth() * 0.80));
        stage.setMinHeight(Math.min(720.0, visualBounds.getHeight() * 0.80));
        stage.setScene(scene);
        stage.setWidth(initialWidth);
        stage.setHeight(initialHeight);
        stage.setX(visualBounds.getMinX() + ((visualBounds.getWidth() - initialWidth) / 2.0));
        stage.setY(visualBounds.getMinY() + ((visualBounds.getHeight() - initialHeight) / 2.0));
        stage.show();
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
