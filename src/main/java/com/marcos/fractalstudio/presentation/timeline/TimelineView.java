package com.marcos.fractalstudio.presentation.timeline;

import com.marcos.fractalstudio.presentation.dialogs.KeyframeLabelDialog;
import com.marcos.fractalstudio.presentation.shell.StudioShellViewModel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class TimelineView extends VBox {

    private final KeyframeLabelDialog keyframeLabelDialog = new KeyframeLabelDialog();

    public TimelineView(StudioShellViewModel viewModel) {
        getStyleClass().add("panel");
        setSpacing(10.0);
        setPadding(new Insets(16.0));

        Label title = new Label("Puntos");
        title.getStyleClass().add("panel-title");

        Label summary = new Label();
        summary.textProperty().bind(viewModel.timelineSummaryProperty());
        summary.getStyleClass().add("panel-subtitle");

        HBox header = new HBox(title, summary);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12.0);

        ListView<KeyframeTimelineItem> listView = new ListView<>(viewModel.timelineItems());
        Label placeholder = new Label("Aun no hay puntos.\nUsa \"Guardar punto\" para capturar la vista actual.");
        placeholder.getStyleClass().add("empty-state-label");
        placeholder.setWrapText(true);
        listView.setPlaceholder(placeholder);
        listView.setCellFactory(ignored -> new ListCell<>() {
            private final ImageView thumbnailView = new ImageView();
            private final Label titleLabel = new Label();
            private final Label detailLabel = new Label();
            private final Label thumbnailStatusLabel = new Label();
            private final Button focusButton = new Button("Ir");
            private final Region spacer = new Region();
            private final VBox textContent = new VBox(4.0, titleLabel, detailLabel, thumbnailStatusLabel);
            private final HBox footer = new HBox(8.0, spacer, focusButton);
            private final VBox body = new VBox(8.0, textContent, footer);
            private final HBox content = new HBox(12.0, thumbnailView, body);
            {
                content.getStyleClass().add("timeline-item");
                titleLabel.getStyleClass().add("timeline-item-title");
                detailLabel.getStyleClass().add("timeline-item-detail");
                detailLabel.setWrapText(true);
                thumbnailStatusLabel.getStyleClass().add("timeline-item-detail");
                thumbnailView.setFitWidth(160.0);
                thumbnailView.setFitHeight(90.0);
                thumbnailView.setPreserveRatio(true);
                thumbnailView.getStyleClass().add("timeline-thumbnail");
                HBox.setHgrow(spacer, Priority.ALWAYS);
                body.setFillWidth(true);
                HBox.setHgrow(body, Priority.ALWAYS);
                focusButton.getStyleClass().add("timeline-focus-button");
            }

            @Override
            protected void updateItem(KeyframeTimelineItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                titleLabel.setText(item.keyframe().label() + "  |  t=" + String.format("%.1f", item.keyframe().seconds()) + "s");
                detailLabel.setText("Centro " + String.format("%.4f", item.keyframe().centerX()) + ", "
                        + String.format("%.4f", item.keyframe().centerY()) + "  |  Zoom " + String.format("%.2f", item.keyframe().zoom()) + "x");
                thumbnailView.imageProperty().unbind();
                thumbnailView.imageProperty().bind(item.thumbnailProperty());
                thumbnailStatusLabel.textProperty().unbind();
                thumbnailStatusLabel.textProperty().bind(item.thumbnailStatusProperty());
                focusButton.setOnAction(event -> viewModel.focusKeyframe(item.keyframe().id()));
                setContextMenu(buildContextMenu(viewModel, item));
                setText(null);
                setGraphic(content);
            }
        });

        VBox.setVgrow(listView, Priority.ALWAYS);
        getChildren().addAll(header, listView);
    }

    private ContextMenu buildContextMenu(StudioShellViewModel viewModel, KeyframeTimelineItem item) {
        MenuItem goToKeyframe = new MenuItem("Ir al punto");
        goToKeyframe.setOnAction(event -> viewModel.focusKeyframe(item.keyframe().id()));

        MenuItem renameKeyframe = new MenuItem("Renombrar punto");
        renameKeyframe.setOnAction(event -> keyframeLabelDialog.show(
                getScene() == null ? null : getScene().getWindow(),
                viewModel.currentKeyframeLabel(item.keyframe().id())
        ).ifPresent(newLabel -> viewModel.renameKeyframe(item.keyframe().id(), newLabel)));

        MenuItem deleteKeyframe = new MenuItem("Eliminar punto");
        deleteKeyframe.setOnAction(event -> viewModel.deleteKeyframe(item.keyframe().id()));

        return new ContextMenu(goToKeyframe, renameKeyframe, deleteKeyframe);
    }
}
