package com.marcos.fractalstudio.presentation.app;

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Applies the desktop window policy for the primary studio stage.
 *
 * <p>The application entry point should not need to know how window bounds, stylesheet registration,
 * icon loading or initial positioning are calculated. Centralizing those rules here makes the startup
 * code smaller and keeps stage behavior coherent across packaging, development and future tests.
 */
public final class StudioStageConfigurator {

    /**
     * Configures the primary stage without showing it yet.
     *
     * <p>This split allows the splash screen to remain the only visible window
     * during startup and lets the caller decide exactly when the main desktop
     * shell should become visible.
     *
     * @param stage primary JavaFX stage
     * @param root application root node
     * @param appProperties runtime settings that define title and initial dimensions
     */
    public void configure(Stage stage, Parent root, AppProperties appProperties) {
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double initialWidth = Math.min(appProperties.initialWidth(), visualBounds.getWidth() * 0.92);
        double initialHeight = Math.min(appProperties.initialHeight(), visualBounds.getHeight() * 0.90);
        Scene scene = new Scene(root, initialWidth, initialHeight);
        scene.getStylesheets().add(getClass().getResource("/assets/styles/studio.css").toExternalForm());

        stage.setTitle(appProperties.applicationTitle());
        stage.getIcons().setAll(new Image(getClass().getResourceAsStream("/assets/icons/app-icon.png")));
        stage.setMinWidth(Math.min(1080.0, visualBounds.getWidth() * 0.80));
        stage.setMinHeight(Math.min(720.0, visualBounds.getHeight() * 0.80));
        stage.setScene(scene);
        stage.setWidth(initialWidth);
        stage.setHeight(initialHeight);
        stage.setX(visualBounds.getMinX() + ((visualBounds.getWidth() - initialWidth) / 2.0));
        stage.setY(visualBounds.getMinY() + ((visualBounds.getHeight() - initialHeight) / 2.0));
    }

    /**
     * Shows an already configured primary stage.
     *
     * @param stage primary JavaFX stage
     */
    public void show(Stage stage) {
        stage.show();
    }
}
