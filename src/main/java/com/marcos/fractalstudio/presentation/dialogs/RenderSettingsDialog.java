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
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public final class RenderSettingsDialog {

    public Optional<RenderSettingsDialogResult> show(
            Window owner,
            String renderName,
            double durationSeconds,
            double framesPerSecond,
            String baseDirectory
    ) {
        Dialog<RenderSettingsDialogResult> dialog = new Dialog<>();
        dialog.setTitle("Render Settings");
        dialog.setHeaderText("Configurar render de secuencia");
        dialog.initOwner(owner);
        DialogStyler.apply(dialog);

        ButtonType runButton = new ButtonType("Renderizar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(runButton, ButtonType.CANCEL);

        TextField renderNameField = new TextField(renderName);
        renderNameField.setTooltip(new Tooltip("Nombre de la carpeta de trabajo que contendra el video y sus frames temporales."));

        Spinner<Double> durationSpinner = new Spinner<>();
        durationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 600.0, durationSeconds, 0.5));
        durationSpinner.setEditable(true);
        durationSpinner.setTooltip(new Tooltip("Duracion total deseada de la animacion."));

        Spinner<Double> fpsSpinner = new Spinner<>();
        fpsSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 120.0, framesPerSecond, 0.5));
        fpsSpinner.setEditable(true);
        fpsSpinner.setTooltip(new Tooltip("Fotogramas por segundo del video final."));

        TextField outputDirectoryField = new TextField(baseDirectory);
        outputDirectoryField.setTooltip(new Tooltip("Carpeta base elegida por ti. El programa creara dentro una carpeta propia del render."));
        javafx.scene.control.Button browseButton = new javafx.scene.control.Button("Elegir...");
        browseButton.getStyleClass().add("dialog-browse-button");
        browseButton.setTooltip(new Tooltip("Seleccionar carpeta base donde se creara el render."));
        browseButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Seleccionar carpeta base del render");
            File initialDirectory = existingDirectory(outputDirectoryField.getText());
            if (initialDirectory != null) {
                directoryChooser.setInitialDirectory(initialDirectory);
            }
            File selectedDirectory = directoryChooser.showDialog(owner);
            if (selectedDirectory != null) {
                outputDirectoryField.setText(selectedDirectory.getAbsolutePath());
            }
        });
        HBox outputDirectoryBox = new HBox(8.0, outputDirectoryField, browseButton);
        HBox.setHgrow(outputDirectoryField, Priority.ALWAYS);
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("dialog-grid");
        gridPane.setHgap(12.0);
        gridPane.setVgap(12.0);
        gridPane.setPadding(new Insets(16.0));
        gridPane.addRow(0, new Label("Nombre render"), renderNameField);
        gridPane.addRow(1, new Label("Duracion (s)"), durationSpinner);
        gridPane.addRow(2, new Label("FPS"), fpsSpinner);
        gridPane.addRow(3, new Label("Carpeta base"), outputDirectoryBox);
        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(buttonType -> buttonType == runButton
                ? new RenderSettingsDialogResult(
                renderNameField.getText(),
                durationSpinner.getValue(),
                fpsSpinner.getValue(),
                outputDirectoryField.getText()
        )
                : null);

        return dialog.showAndWait();
    }

    private File existingDirectory(String pathText) {
        if (pathText == null || pathText.isBlank()) {
            return null;
        }
        Path path = Path.of(pathText);
        Path candidate = path;
        if (!java.nio.file.Files.isDirectory(candidate)) {
            candidate = candidate.getParent();
        }
        if (candidate == null || !java.nio.file.Files.isDirectory(candidate)) {
            return null;
        }
        return candidate.toFile();
    }

}
