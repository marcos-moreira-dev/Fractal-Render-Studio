package com.marcos.fractalstudio.presentation.dialogs;

import com.marcos.fractalstudio.domain.render.RenderPreset;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.util.Optional;

public final class ProjectSettingsDialog {

    public Optional<ProjectSettingsDialogResult> show(
            Window owner,
            String projectName,
            String description,
            double defaultFramesPerSecond,
            int defaultRenderFrameCount,
            double keyframeStepSeconds,
            String defaultRenderPreset
    ) {
        Dialog<ProjectSettingsDialogResult> dialog = new Dialog<>();
        dialog.setTitle("Project Settings");
        dialog.setHeaderText("Configurar metadata y settings del proyecto");
        dialog.initOwner(owner);
        DialogStyler.apply(dialog);

        ButtonType saveButton = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField projectNameField = new TextField(projectName);
        projectNameField.setTooltip(new Tooltip("Nombre visible del proyecto en la interfaz."));
        TextArea descriptionArea = new TextArea(description);
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setTooltip(new Tooltip("Descripcion corta del proyecto o de la exploracion fractal."));

        Spinner<Double> fpsSpinner = new Spinner<>();
        fpsSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 120.0, defaultFramesPerSecond, 0.5));
        fpsSpinner.setEditable(true);
        fpsSpinner.setTooltip(new Tooltip("Fotogramas por segundo usados por defecto al renderizar."));

        Spinner<Double> durationSpinner = new Spinner<>();
        durationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.1,
                600.0,
                defaultRenderFrameCount / Math.max(0.0001, defaultFramesPerSecond),
                0.5
        ));
        durationSpinner.setEditable(true);
        durationSpinner.setTooltip(new Tooltip("Duracion por defecto de la animacion en segundos."));

        Spinner<Double> keyframeStepSpinner = new Spinner<>();
        keyframeStepSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 60.0, keyframeStepSeconds, 0.5));
        keyframeStepSpinner.setEditable(true);
        keyframeStepSpinner.setTooltip(new Tooltip("Separacion sugerida entre keyframes nuevos en la timeline."));

        ComboBox<String> renderPresetComboBox = new ComboBox<>();
        renderPresetComboBox.getItems().addAll(java.util.Arrays.stream(RenderPreset.values()).map(Enum::name).toList());
        renderPresetComboBox.setValue(defaultRenderPreset);
        renderPresetComboBox.setTooltip(new Tooltip("Preset de calidad por defecto para renders finales."));

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("dialog-grid");
        gridPane.setHgap(12.0);
        gridPane.setVgap(12.0);
        gridPane.setPadding(new Insets(16.0));
        gridPane.addRow(0, new Label("Nombre"), projectNameField);
        gridPane.addRow(1, new Label("Descripcion"), descriptionArea);
        gridPane.addRow(2, new Label("FPS default"), fpsSpinner);
        gridPane.addRow(3, new Label("Duracion default (s)"), durationSpinner);
        gridPane.addRow(4, new Label("Paso keyframe (s)"), keyframeStepSpinner);
        gridPane.addRow(5, new Label("Preset render"), renderPresetComboBox);
        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(buttonType -> buttonType == saveButton
                ? new ProjectSettingsDialogResult(
                projectNameField.getText(),
                descriptionArea.getText(),
                fpsSpinner.getValue(),
                durationSpinner.getValue(),
                keyframeStepSpinner.getValue(),
                renderPresetComboBox.getValue()
        )
                : null);

        return dialog.showAndWait();
    }
}
