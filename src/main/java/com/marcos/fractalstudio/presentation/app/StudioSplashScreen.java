package com.marcos.fractalstudio.presentation.app;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Lightweight splash screen shown while the JavaFX desktop shell is being
 * assembled.
 *
 * <p>The splash is intentionally independent from the main stage. It gives the
 * user immediate visual feedback, displays the horizontal product mark and then
 * disappears once the primary studio window is ready. A small minimum display
 * time keeps the splash from flashing too quickly on fast machines.
 */
public final class StudioSplashScreen {

    private static final Duration MINIMUM_VISIBLE_DURATION = Duration.seconds(5);
    private static final double SPLASH_WIDTH = 520.0;
    private static final double SPLASH_HEIGHT = 250.0;

    private final Stage stage = new Stage(StageStyle.TRANSPARENT);
    private final long shownAtNanos;

    /**
     * Creates and shows the splash screen.
     */
    public StudioSplashScreen() {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setScene(buildScene());
        centerOnPrimaryScreen();
        stage.show();
        shownAtNanos = System.nanoTime();
    }

    /**
     * Closes the splash screen while honoring a short minimum visible time and
     * then executes the supplied callback.
     *
     * @param onClosed action to run after the splash disappears
     */
    public void closeWhenReady(Runnable onClosed) {
        long elapsedNanos = System.nanoTime() - shownAtNanos;
        Duration remaining = MINIMUM_VISIBLE_DURATION.subtract(Duration.millis(elapsedNanos / 1_000_000.0));
        if (remaining.lessThanOrEqualTo(Duration.ZERO)) {
            stage.close();
            onClosed.run();
            return;
        }
        PauseTransition pauseTransition = new PauseTransition(remaining);
        pauseTransition.setOnFinished(event -> {
            stage.close();
            onClosed.run();
        });
        pauseTransition.play();
    }

    private Scene buildScene() {
        ImageView wordmarkView = new ImageView(new Image(
                StudioSplashScreen.class.getResourceAsStream("/assets/images/app-wordmark.png")
        ));
        wordmarkView.setPreserveRatio(true);
        wordmarkView.setFitWidth(420.0);
        wordmarkView.setSmooth(true);

        Label subtitle = new Label("Inicializando explorador fractal y pipeline de render");
        subtitle.setStyle("""
                -fx-text-fill: #E8F0FF;
                -fx-font-size: 15px;
                -fx-font-weight: 500;
                -fx-opacity: 0.92;
                """);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1.0);
        progressIndicator.setPrefSize(34.0, 34.0);
        progressIndicator.setStyle("""
                -fx-progress-color: #4DA8FF;
                """);

        Region separator = new Region();
        separator.setPrefHeight(4.0);

        VBox root = new VBox(18.0, wordmarkView, subtitle, separator, progressIndicator);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(34.0, 42.0, 34.0, 42.0));
        root.setStyle("""
                -fx-background-color: linear-gradient(to bottom right, rgba(8,16,30,0.97), rgba(17,29,52,0.95));
                -fx-background-radius: 24;
                -fx-border-color: rgba(120,170,255,0.18);
                -fx-border-width: 1;
                -fx-border-radius: 24;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 28, 0.25, 0, 10);
                """);
        root.setMinSize(SPLASH_WIDTH, SPLASH_HEIGHT);

        Scene scene = new Scene(root, SPLASH_WIDTH, SPLASH_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    private void centerOnPrimaryScreen() {
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX() + (bounds.getWidth() - SPLASH_WIDTH) / 2.0);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - SPLASH_HEIGHT) / 2.0);
    }
}
