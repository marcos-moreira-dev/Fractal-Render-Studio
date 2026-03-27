package com.marcos.fractalstudio.presentation.dialogs;

import com.marcos.fractalstudio.presentation.renderqueue.RenderJobRow;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public final class RenderJobDetailsDialog {

    public void show(Window owner, RenderJobRow renderJobRow) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Render Job Details");
        alert.setHeaderText("Detalle del job de render");
        alert.initOwner(owner);
        DialogStyler.apply(alert);

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("dialog-grid");
        gridPane.setHgap(12.0);
        gridPane.setVgap(12.0);
        gridPane.setPadding(new Insets(16.0));
        gridPane.addRow(0, new Label("Job ID"), new Label(renderJobRow.jobId()));
        gridPane.addRow(1, new Label("Nombre"), new Label(renderJobRow.jobName()));
        gridPane.addRow(2, new Label("Estado"), new Label(renderJobRow.state()));
        gridPane.addRow(3, new Label("Progreso"), new Label(String.format("%.2f%%", renderJobRow.progress() * 100.0)));
        gridPane.addRow(4, new Label("Mensaje"), new Label(renderJobRow.message()));

        alert.getDialogPane().setContent(gridPane);
        alert.showAndWait();
    }
}
