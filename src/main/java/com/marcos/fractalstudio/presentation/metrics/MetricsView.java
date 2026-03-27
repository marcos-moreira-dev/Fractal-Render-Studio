package com.marcos.fractalstudio.presentation.metrics;

import com.marcos.fractalstudio.presentation.shell.StudioShellViewModel;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class MetricsView extends VBox {

    public MetricsView(StudioShellViewModel viewModel) {
        getStyleClass().add("panel");
        setSpacing(10.0);
        setPadding(new Insets(16.0));

        Label title = new Label("Metrics & Logs");
        title.getStyleClass().add("panel-title");

        TextArea textArea = new TextArea();
        textArea.textProperty().bind(viewModel.metricsTextProperty());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        VBox.setVgrow(textArea, Priority.ALWAYS);
        getChildren().addAll(title, textArea);
    }
}
