package com.marcos.fractalstudio.presentation.explorer;

import com.marcos.fractalstudio.presentation.shell.StudioShellViewModel;

import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Interactive fractal canvas with pan, zoom and transient zoom-target feedback.
 */
public final class FractalExplorerView extends StackPane {

    private static final double MIN_STARTUP_PREVIEW_WIDTH = 320.0;
    private static final double MIN_STARTUP_PREVIEW_HEIGHT = 220.0;

    private double lastMouseX;
    private double lastMouseY;
    private boolean startupPreviewRequested;

    /**
     * Creates the explorer surface bound to the shared shell view model.
     *
     * @param viewModel shell state and commands backing the explorer
     */
    public FractalExplorerView(
            StudioShellViewModel viewModel,
            ObservableBooleanValue bottomPanelVisible,
            Runnable toggleBottomPanel
    ) {
        getStyleClass().add("explorer-panel");
        setMinSize(0.0, 0.0);
        PauseTransition interactionPreviewDelay = new PauseTransition(Duration.millis(240));
        interactionPreviewDelay.setOnFinished(event -> viewModel.requestPreviewAfterInteraction());
        PauseTransition zoomTargetHold = new PauseTransition(Duration.seconds(3));

        ImageView imageView = new ImageView();
        imageView.setManaged(false);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());
        imageView.imageProperty().bind(viewModel.previewImageProperty());

        Rectangle drawerScrim = new Rectangle();
        drawerScrim.setManaged(false);
        drawerScrim.widthProperty().bind(widthProperty());
        drawerScrim.heightProperty().bind(heightProperty());
        drawerScrim.getStyleClass().add("explorer-drawer-scrim");
        drawerScrim.visibleProperty().bind(bottomPanelVisible);
        drawerScrim.setMouseTransparent(true);

        Circle zoomTargetRing = new Circle(24.0);
        zoomTargetRing.getStyleClass().add("zoom-target-ring");

        Circle zoomTargetDot = new Circle(4.5);
        zoomTargetDot.getStyleClass().add("zoom-target-dot");

        StackPane zoomTarget = new StackPane(zoomTargetRing, zoomTargetDot);
        zoomTarget.setMouseTransparent(true);
        zoomTarget.setVisible(false);
        zoomTarget.setOpacity(0.0);

        FadeTransition zoomTargetFadeOut = new FadeTransition(Duration.millis(420), zoomTarget);
        zoomTargetFadeOut.setFromValue(1.0);
        zoomTargetFadeOut.setToValue(0.0);
        zoomTargetFadeOut.setOnFinished(event -> zoomTarget.setVisible(false));

        zoomTargetHold.setOnFinished(event -> zoomTargetFadeOut.playFromStart());

        Label statusLabel = new Label();
        statusLabel.textProperty().bind(viewModel.viewportStatusProperty());
        statusLabel.getStyleClass().add("overlay-label");

        Label hintLabel = new Label("Drag: paneo  |  Scroll: zoom");
        hintLabel.getStyleClass().add("overlay-label");

        Label previewModeLabel = new Label();
        previewModeLabel.textProperty().bind(viewModel.previewModeLabelProperty());
        previewModeLabel.getStyleClass().add("overlay-label");

        Label cameraLabel = new Label();
        cameraLabel.textProperty().bind(viewModel.cameraCenterLabelProperty().concat("  |  ").concat(viewModel.cameraZoomLabelProperty()));
        cameraLabel.getStyleClass().add("overlay-label");

        VBox overlay = new VBox(6.0, statusLabel, previewModeLabel, hintLabel);
        overlay.getStyleClass().add("explorer-overlay");
        overlay.setMouseTransparent(true);
        overlay.setPadding(new Insets(16.0));
        overlay.setAlignment(Pos.TOP_LEFT);

        Button panelToggleButton = buildExplorerActionButton("Timeline", toggleBottomPanel);
        panelToggleButton.textProperty().bind(Bindings.when(bottomPanelVisible).then("Timeline -").otherwise("Timeline +"));
        panelToggleButton.setTooltip(new Tooltip("Muestra u oculta el drawer de Timeline y Render Queue"));
        bottomPanelVisible.addListener((ignored, oldValue, visible) -> updatePanelToggleStyle(panelToggleButton, visible));
        updatePanelToggleStyle(panelToggleButton, bottomPanelVisible.get());

        Button zoomOutButton = buildExplorerActionButton("🔍-", () -> {
            boolean zoomApplied = viewModel.zoomOut();
            keepZoomTargetVisible(
                    zoomTarget,
                    zoomTargetRing,
                    zoomTargetDot,
                    zoomTargetFadeOut,
                    zoomTargetHold,
                    getWidth() / 2.0,
                    getHeight() / 2.0,
                    !zoomApplied
            );
        });
        zoomOutButton.setTooltip(new Tooltip("Aleja el viewport actual"));
        Button zoomInButton = buildExplorerActionButton("🔍+", () -> {
            boolean zoomApplied = viewModel.zoomIn();
            keepZoomTargetVisible(
                    zoomTarget,
                    zoomTargetRing,
                    zoomTargetDot,
                    zoomTargetFadeOut,
                    zoomTargetHold,
                    getWidth() / 2.0,
                    getHeight() / 2.0,
                    !zoomApplied
            );
        });
        zoomInButton.setTooltip(new Tooltip("Acerca el viewport actual"));
        Button resetButton = buildExplorerActionButton("Inicio", viewModel::resetCamera);
        resetButton.setTooltip(new Tooltip("Vuelve al encuadre inicial"));
        Button previewButton = buildExplorerActionButton("Refinar", viewModel::requestManualPreview);
        previewButton.setTooltip(new Tooltip("Recalcula el viewport actual con mayor precision visual"));
        Button addBookmarkButton = buildExplorerActionButton("Guardar punto", viewModel::addPoint);
        addBookmarkButton.setTooltip(new Tooltip("Guarda el punto actual y lo agrega al recorrido de animacion"));
        Button previousBookmarkButton = buildExplorerActionButton("<", viewModel::jumpToPreviousBookmark);
        previousBookmarkButton.setTooltip(new Tooltip("Carga el punto anterior"));
        Button nextBookmarkButton = buildExplorerActionButton(">", viewModel::jumpToNextBookmark);
        nextBookmarkButton.setTooltip(new Tooltip("Carga el siguiente punto"));

        HBox quickActions = new HBox(
                8.0,
                panelToggleButton,
                previousBookmarkButton,
                nextBookmarkButton,
                addBookmarkButton,
                zoomOutButton,
                zoomInButton,
                resetButton,
                previewButton
        );
        quickActions.getStyleClass().add("explorer-quick-actions");
        quickActions.setPadding(new Insets(16.0));
        quickActions.setAlignment(Pos.TOP_RIGHT);

        VBox bottomOverlay = new VBox(8.0, cameraLabel);
        bottomOverlay.getStyleClass().add("explorer-overlay");
        bottomOverlay.setMouseTransparent(true);
        bottomOverlay.setPadding(new Insets(16.0));
        bottomOverlay.setAlignment(Pos.BOTTOM_LEFT);

        getChildren().addAll(imageView, drawerScrim, zoomTarget, overlay, quickActions, bottomOverlay);
        centerImageView(imageView);
        StackPane.setAlignment(drawerScrim, Pos.CENTER);
        StackPane.setAlignment(zoomTarget, Pos.CENTER);
        StackPane.setAlignment(overlay, Pos.TOP_LEFT);
        StackPane.setAlignment(quickActions, Pos.TOP_RIGHT);
        StackPane.setAlignment(bottomOverlay, Pos.BOTTOM_LEFT);

        widthProperty().addListener((ignored, oldValue, newValue) -> {
            viewModel.updateViewportSize(newValue.doubleValue(), getHeight());
        });
        heightProperty().addListener((ignored, oldValue, newValue) -> {
            viewModel.updateViewportSize(getWidth(), newValue.doubleValue());
        });

        installStartupPreview(viewModel);

        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            requestFocus();
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            viewModel.panCamera(deltaX, deltaY);
            keepZoomTargetVisible(zoomTarget, zoomTargetRing, zoomTargetDot, zoomTargetFadeOut, zoomTargetHold);
        });

        addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            if (zoomTarget.isVisible()) {
                keepZoomTargetVisible(zoomTarget, zoomTargetRing, zoomTargetDot, zoomTargetFadeOut, zoomTargetHold);
            }
        });

        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> viewModel.requestPreviewAfterInteraction());

        setOnScroll(event -> {
            double factor = event.getDeltaY() > 0 ? 1.18 : 0.85;
            boolean zoomApplied = viewModel.zoomCameraAt(factor, event.getX(), event.getY());
            keepZoomTargetVisible(
                    zoomTarget,
                    zoomTargetRing,
                    zoomTargetDot,
                    zoomTargetFadeOut,
                    zoomTargetHold,
                    event.getX(),
                    event.getY(),
                    !zoomApplied
            );
            if (zoomApplied) {
                interactionPreviewDelay.playFromStart();
            }
            event.consume();
        });

        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, event -> handleKeyboardShortcut(event, viewModel));
    }

    private void centerImageView(ImageView imageView) {
        Runnable relocate = () -> {
            double imageWidth = imageView.getBoundsInLocal().getWidth();
            double imageHeight = imageView.getBoundsInLocal().getHeight();
            imageView.relocate(
                    Math.max(0.0, (getWidth() - imageWidth) / 2.0),
                    Math.max(0.0, (getHeight() - imageHeight) / 2.0)
            );
        };
        widthProperty().addListener((ignored, oldValue, newValue) -> relocate.run());
        heightProperty().addListener((ignored, oldValue, newValue) -> relocate.run());
        imageView.boundsInLocalProperty().addListener((ignored, oldBounds, newBounds) -> relocate.run());
    }

    private Button buildExplorerActionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("explorer-action-button");
        button.setOnAction(event -> action.run());
        return button;
    }

    private void updatePanelToggleStyle(Button panelToggleButton, boolean visible) {
        if (visible) {
            if (!panelToggleButton.getStyleClass().contains("explorer-action-button-active")) {
                panelToggleButton.getStyleClass().add("explorer-action-button-active");
            }
            return;
        }
        panelToggleButton.getStyleClass().remove("explorer-action-button-active");
    }

    private void installStartupPreview(StudioShellViewModel viewModel) {
        PauseTransition startupPreviewDelay = new PauseTransition(Duration.millis(480));
        startupPreviewDelay.setOnFinished(event -> {
            if (startupPreviewRequested || !isReadyForStartupPreview()) {
                return;
            }
            startupPreviewRequested = true;
            viewModel.requestAutoPreview();
        });
        widthProperty().addListener((ignored, oldValue, newValue) -> scheduleStartupPreviewIfReady(startupPreviewDelay));
        heightProperty().addListener((ignored, oldValue, newValue) -> scheduleStartupPreviewIfReady(startupPreviewDelay));
        sceneProperty().addListener((ignored, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }
            newScene.windowProperty().addListener((ignoredWindow, oldWindow, newWindow) -> {
                if (newWindow == null || startupPreviewRequested) {
                    return;
                }
                newWindow.showingProperty().addListener((ignoredShowing, wasShowing, isShowing) -> {
                    if (!isShowing || startupPreviewRequested) {
                        return;
                    }
                    Platform.runLater(() -> scheduleStartupPreviewIfReady(startupPreviewDelay));
                });
            });
        });
    }

    private void scheduleStartupPreviewIfReady(PauseTransition startupPreviewDelay) {
        if (startupPreviewRequested || !isReadyForStartupPreview()) {
            return;
        }
        startupPreviewDelay.playFromStart();
    }

    private boolean isReadyForStartupPreview() {
        return getWidth() >= MIN_STARTUP_PREVIEW_WIDTH
                && getHeight() >= MIN_STARTUP_PREVIEW_HEIGHT
                && getScene() != null
                && getScene().getWindow() != null
                && getScene().getWindow().isShowing();
    }

    private void keepZoomTargetVisible(
            StackPane zoomTarget,
            Circle zoomTargetRing,
            Circle zoomTargetDot,
            FadeTransition zoomTargetFadeOut,
            PauseTransition zoomTargetHold
    ) {
        keepZoomTargetVisible(
                zoomTarget,
                zoomTargetRing,
                zoomTargetDot,
                zoomTargetFadeOut,
                zoomTargetHold,
                getWidth() / 2.0,
                getHeight() / 2.0,
                false
        );
    }

    private void keepZoomTargetVisible(
            StackPane zoomTarget,
            Circle zoomTargetRing,
            Circle zoomTargetDot,
            FadeTransition zoomTargetFadeOut,
            PauseTransition zoomTargetHold,
            double anchorX,
            double anchorY
    ) {
        keepZoomTargetVisible(
                zoomTarget,
                zoomTargetRing,
                zoomTargetDot,
                zoomTargetFadeOut,
                zoomTargetHold,
                anchorX,
                anchorY,
                false
        );
    }

    private void keepZoomTargetVisible(
            StackPane zoomTarget,
            Circle zoomTargetRing,
            Circle zoomTargetDot,
            FadeTransition zoomTargetFadeOut,
            PauseTransition zoomTargetHold,
            double anchorX,
            double anchorY,
            boolean blocked
    ) {
        zoomTargetFadeOut.stop();
        zoomTargetHold.stop();
        updateZoomTargetStyle(zoomTargetRing, zoomTargetDot, blocked);
        positionZoomTarget(zoomTarget, anchorX, anchorY);
        zoomTarget.setVisible(true);
        zoomTarget.setOpacity(1.0);
        zoomTargetHold.playFromStart();
    }

    private void updateZoomTargetStyle(Circle zoomTargetRing, Circle zoomTargetDot, boolean blocked) {
        if (blocked) {
            if (!zoomTargetRing.getStyleClass().contains("zoom-target-ring-blocked")) {
                zoomTargetRing.getStyleClass().add("zoom-target-ring-blocked");
            }
            if (!zoomTargetDot.getStyleClass().contains("zoom-target-dot-blocked")) {
                zoomTargetDot.getStyleClass().add("zoom-target-dot-blocked");
            }
            return;
        }
        zoomTargetRing.getStyleClass().remove("zoom-target-ring-blocked");
        zoomTargetDot.getStyleClass().remove("zoom-target-dot-blocked");
    }

    private void positionZoomTarget(StackPane zoomTarget, double anchorX, double anchorY) {
        double boundedX = Math.max(0.0, Math.min(getWidth(), anchorX));
        double boundedY = Math.max(0.0, Math.min(getHeight(), anchorY));
        zoomTarget.setTranslateX(boundedX - (getWidth() / 2.0));
        zoomTarget.setTranslateY(boundedY - (getHeight() / 2.0));
    }

    private void handleKeyboardShortcut(KeyEvent event, StudioShellViewModel viewModel) {
        if (event.getCode() == KeyCode.ADD || event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS) {
            viewModel.zoomIn();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.SUBTRACT || event.getCode() == KeyCode.MINUS) {
            viewModel.zoomOut();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.HOME || event.getCode() == KeyCode.DIGIT0) {
            viewModel.resetCamera();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.P || event.getCode() == KeyCode.SPACE) {
            viewModel.requestManualPreview();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.B) {
            viewModel.addPoint();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.PAGE_UP) {
            viewModel.jumpToPreviousBookmark();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.PAGE_DOWN) {
            viewModel.jumpToNextBookmark();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.LEFT) {
            viewModel.nudgeCameraLeft();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.RIGHT) {
            viewModel.nudgeCameraRight();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.UP) {
            viewModel.nudgeCameraUp();
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.DOWN) {
            viewModel.nudgeCameraDown();
            event.consume();
        }
    }
}
