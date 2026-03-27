package com.marcos.fractalstudio.presentation.metrics;

import com.marcos.fractalstudio.presentation.renderqueue.RenderQueueView;
import com.marcos.fractalstudio.presentation.shell.StudioShellViewModel;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public final class MetricsScreenView extends BorderPane {

    public MetricsScreenView(StudioShellViewModel viewModel) {
        getStyleClass().add("workspace-root");
        getStyleClass().add("metrics-screen");
        setPadding(new Insets(12.0));

        Label title = new Label("Observabilidad");
        title.getStyleClass().add("screen-title");
        setTop(title);

        SplitPane splitPane = new SplitPane(
                new MetricsView(viewModel),
                new RenderQueueView(viewModel)
        );
        splitPane.setDividerPositions(0.56);
        setCenter(splitPane);
    }
}
