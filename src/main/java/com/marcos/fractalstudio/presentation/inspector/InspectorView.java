package com.marcos.fractalstudio.presentation.inspector;

import com.marcos.fractalstudio.presentation.shell.StudioShellViewModel;

import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;

public final class InspectorView extends VBox {

    public InspectorView(StudioShellViewModel viewModel) {
        getStyleClass().add("panel");
        setPrefWidth(300.0);
        setFillWidth(true);

        VBox content = new VBox(12.0);
        content.setPadding(new Insets(16.0));
        content.getStyleClass().add("inspector-content");

        Label title = new Label("Inspector");
        title.getStyleClass().add("panel-title");

        Label subtitle = new Label("Ajusta formula, color e iteraciones sin salir del viewport.");
        subtitle.getStyleClass().add("panel-subtitle");

        ComboBox<String> formulaComboBox = new ComboBox<>(FXCollections.observableArrayList(viewModel.availableFractalFormulas()));
        formulaComboBox.getStyleClass().add("inspector-combo-box");
        formulaComboBox.setMaxWidth(Double.MAX_VALUE);
        formulaComboBox.setValue(viewModel.currentFractalFormulaName());
        viewModel.formulaNameProperty().addListener((ignored, oldValue, newValue) -> formulaComboBox.setValue(newValue));

        ComboBox<String> paletteComboBox = new ComboBox<>(FXCollections.observableArrayList(viewModel.availableColorProfiles()));
        paletteComboBox.getStyleClass().add("inspector-combo-box");
        paletteComboBox.setMaxWidth(Double.MAX_VALUE);
        paletteComboBox.setValue(viewModel.currentColorProfileName());
        viewModel.colorProfileNameProperty().addListener((ignored, oldValue, newValue) -> paletteComboBox.setValue(newValue));

        Spinner<Integer> iterationsSpinner = new Spinner<>();
        iterationsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(32, 255, viewModel.currentMaxIterations(), 1));
        iterationsSpinner.getStyleClass().add("inspector-spinner");
        iterationsSpinner.setEditable(true);
        iterationsSpinner.setMaxWidth(Double.MAX_VALUE);
        viewModel.configuredMaxIterationsProperty().addListener((ignored, oldValue, newValue) ->
                iterationsSpinner.getValueFactory().setValue(newValue.intValue()));

        Spinner<Double> escapeRadiusSpinner = new Spinner<>();
        escapeRadiusSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(2.0, 16.0, viewModel.currentEscapeRadius(), 0.5));
        escapeRadiusSpinner.getStyleClass().add("inspector-spinner");
        escapeRadiusSpinner.setEditable(true);
        escapeRadiusSpinner.setMaxWidth(Double.MAX_VALUE);
        viewModel.configuredEscapeRadiusProperty().addListener((ignored, oldValue, newValue) ->
                escapeRadiusSpinner.getValueFactory().setValue(newValue.doubleValue()));

        Button applyButton = new Button("Aplicar cambios");
        applyButton.getStyleClass().add("inspector-action-button");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setOnAction(event -> viewModel.applyInspectorSettings(
                formulaComboBox.getValue(),
                paletteComboBox.getValue(),
                iterationsSpinner.getValue(),
                escapeRadiusSpinner.getValue()
        ));

        content.getChildren().addAll(
                title,
                subtitle,
                buildEditor("Formula", formulaComboBox),
                buildEditor("Paleta", paletteComboBox),
                buildEditor("Iteraciones", iterationsSpinner),
                buildEditor("Escape radius", escapeRadiusSpinner),
                applyButton,
                buildValue("Proyecto", viewModel.projectNameProperty()),
                buildValue("Resolucion", viewModel.resolutionLabelProperty()),
                buildValue("Centro", viewModel.cameraCenterLabelProperty()),
                buildValue("Zoom", viewModel.cameraZoomLabelProperty()),
                buildValue("Modo preview", viewModel.previewModeLabelProperty()),
                buildValue("Estado deep zoom", viewModel.deepZoomHealthLabelProperty()),
                buildValue("Memoria JVM", viewModel.deepZoomMemoryLabelProperty()),
                buildValue("Preview", viewModel.previewIterationsLabelProperty()),
                buildValue("Preset render", viewModel.defaultRenderPresetLabelProperty()),
                buildValue("Puntos", viewModel.bookmarkSummaryProperty()),
                buildValue("FPS default", viewModel.projectDefaultFpsProperty()),
                buildValue("Creado", viewModel.projectCreatedAtProperty()),
                buildValue("Actualizado", viewModel.projectUpdatedAtProperty()),
                buildValue("Descripcion", viewModel.projectDescriptionProperty())
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("inspector-scroll");
        getChildren().add(scrollPane);
    }

    private VBox buildEditor(String title, javafx.scene.Node control) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("inspector-label");

        VBox section = new VBox(6.0, titleLabel, control);
        section.getStyleClass().add("inspector-section");
        return section;
    }

    private VBox buildValue(String title, ObservableStringValue value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("inspector-label");

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(value);
        valueLabel.getStyleClass().add("inspector-value");
        valueLabel.setWrapText(true);

        VBox section = new VBox(4.0, titleLabel, valueLabel);
        section.getStyleClass().add("inspector-section");
        return section;
    }
}
