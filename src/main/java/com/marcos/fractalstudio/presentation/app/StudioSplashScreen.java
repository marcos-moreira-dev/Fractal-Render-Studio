package com.marcos.fractalstudio.presentation.app;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private static final double SPLASH_WIDTH = 560.0;
    private static final double SPLASH_HEIGHT = 470.0;
    private static final Rectangle2D WORDMARK_VIEWPORT = new Rectangle2D(70.0, 25.0, 1400.0, 760.0);

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
        wordmarkView.setViewport(WORDMARK_VIEWPORT);
        wordmarkView.setPreserveRatio(true);
        wordmarkView.setFitWidth(470.0);
        wordmarkView.setFitHeight(245.0);
        wordmarkView.setSmooth(true);

        Label subtitle = new Label("Inicializando explorador fractal y pipeline de render");
        subtitle.setStyle("""
                -fx-text-fill: #E8F0FF;
                -fx-font-size: 15px;
                -fx-font-weight: 500;
                -fx-opacity: 0.92;
                """);
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setMaxWidth(420.0);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1.0);
        progressIndicator.setPrefSize(56.0, 56.0);
        progressIndicator.setMinSize(56.0, 56.0);
        progressIndicator.setMaxSize(56.0, 56.0);
        progressIndicator.setStyle("""
                -fx-progress-color: #4DA8FF;
                -fx-opacity: 1.0;
                """);

        Label loadingLabel = new Label("Cargando interfaz y servicios...");
        loadingLabel.setContentDisplay(ContentDisplay.RIGHT);
        loadingLabel.setStyle("""
                -fx-text-fill: #F6FAFF;
                -fx-font-size: 14px;
                -fx-font-weight: 600;
                """);

        HBox loadingRow = new HBox(14.0, progressIndicator, loadingLabel);
        loadingRow.setAlignment(Pos.CENTER);
        loadingRow.setPadding(new Insets(2.0, 16.0, 0.0, 16.0));

        VBox lowerBlock = new VBox(18.0, subtitle, loadingRow);
        lowerBlock.setAlignment(Pos.CENTER);
        lowerBlock.setMaxWidth(Double.MAX_VALUE);
        lowerBlock.setPadding(new Insets(0.0, 0.0, 10.0, 0.0));

        VBox root = new VBox(0.0, wordmarkView, lowerBlock);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24.0, 34.0, 28.0, 34.0));
        root.setStyle("""
                -fx-background-color: linear-gradient(to bottom right, rgba(8,16,30,0.97), rgba(17,29,52,0.95));
                -fx-background-radius: 24;
                -fx-border-color: rgba(120,170,255,0.18);
                -fx-border-width: 1;
                -fx-border-radius: 24;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 28, 0.25, 0, 10);
                """);
        root.setMinSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        VBox.setMargin(wordmarkView, new Insets(0.0, 0.0, 90.0, 0.0));

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
