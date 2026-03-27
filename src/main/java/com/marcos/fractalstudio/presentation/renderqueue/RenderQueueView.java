package com.marcos.fractalstudio.presentation.renderqueue;

import com.marcos.fractalstudio.presentation.dialogs.RenderJobDetailsDialog;
import com.marcos.fractalstudio.presentation.shell.StudioShellViewModel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class RenderQueueView extends VBox {

    public RenderQueueView(StudioShellViewModel viewModel) {
        getStyleClass().add("panel");
        setSpacing(10.0);
        setPadding(new Insets(16.0));

        Label title = new Label("Render Queue");
        title.getStyleClass().add("panel-title");

        Label helper = new Label("Supervisa el render en curso y revisa su estado sin salir del espacio de trabajo.");
        helper.getStyleClass().add("panel-subtitle");
        helper.setWrapText(true);
        helper.setMaxWidth(Double.MAX_VALUE);

        TableView<RenderJobRow> tableView = new TableView<>(viewModel.renderJobs());
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        Label placeholder = new Label("No hay trabajos de render.\nLanza uno desde \"Render...\" en la barra superior.");
        placeholder.getStyleClass().add("empty-state-label");
        placeholder.setWrapText(true);
        tableView.setPlaceholder(placeholder);

        Button refreshButton = new Button("Actualizar");
        refreshButton.setOnAction(event -> viewModel.refreshRenderJobs());

        Button cancelButton = new Button("Cancelar seleccionado");
        cancelButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        cancelButton.setOnAction(event -> viewModel.cancelRenderJob(tableView.getSelectionModel().getSelectedItem()));

        Button detailsButton = new Button("Detalle");
        detailsButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        detailsButton.setOnAction(event -> {
            RenderJobRow selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                new RenderJobDetailsDialog().show(getScene().getWindow(), selected);
            }
        });

        Region actionsSpacer = new Region();
        HBox.setHgrow(actionsSpacer, Priority.ALWAYS);
        HBox actions = new HBox(8.0, refreshButton, cancelButton, detailsButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        TableColumn<RenderJobRow, String> jobColumn = new TableColumn<>("Job");
        jobColumn.setCellValueFactory(cellData -> cellData.getValue().jobNameProperty());
        jobColumn.setPrefWidth(280.0);

        TableColumn<RenderJobRow, String> stateColumn = new TableColumn<>("Estado");
        stateColumn.setCellValueFactory(cellData -> cellData.getValue().stateProperty());
        stateColumn.setPrefWidth(140.0);

        TableColumn<RenderJobRow, Number> progressColumn = new TableColumn<>("Progreso");
        progressColumn.setCellValueFactory(cellData -> cellData.getValue().progressProperty());
        progressColumn.setCellFactory(ignored -> new TableCell<>() {
            private final ProgressBar progressBar = new ProgressBar();

            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                progressBar.setProgress(item.doubleValue());
                progressBar.setMaxWidth(Double.MAX_VALUE);
                setText(String.format("%.0f%%", item.doubleValue() * 100.0));
                setGraphic(progressBar);
            }
        });
        progressColumn.setPrefWidth(220.0);

        tableView.setRowFactory(ignored -> {
            javafx.scene.control.TableRow<RenderJobRow> row = new javafx.scene.control.TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setTooltip(null);
                } else {
                    row.setTooltip(new javafx.scene.control.Tooltip(newItem.message()));
                }
            });
            return row;
        });

        tableView.getColumns().addAll(jobColumn, stateColumn, progressColumn);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        getChildren().addAll(title, helper, actions, tableView);
    }
}
