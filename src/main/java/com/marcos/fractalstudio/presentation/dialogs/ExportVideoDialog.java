package com.marcos.fractalstudio.presentation.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public final class ExportVideoDialog {

    public Optional<ExportVideoDialogResult> show(Window owner, String defaultDestinationVideo) {
        Dialog<ExportVideoDialogResult> dialog = new Dialog<>();
        dialog.setTitle("Export Video");
        dialog.setHeaderText("Guardar video MP4 renderizado");
        dialog.initOwner(owner);
        DialogStyler.apply(dialog);

        ButtonType exportButton = new ButtonType("Guardar video", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(exportButton, ButtonType.CANCEL);

        TextField destinationField = new TextField(defaultDestinationVideo);
        javafx.scene.control.Button browseButton = new javafx.scene.control.Button("Elegir...");
        browseButton.getStyleClass().add("dialog-browse-button");
        browseButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar destino MP4");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video MP4", "*.mp4"));
            File initialDirectory = existingParent(destinationField.getText());
            if (initialDirectory != null) {
                fileChooser.setInitialDirectory(initialDirectory);
            }
            String initialName = fileName(destinationField.getText());
            if (!initialName.isBlank()) {
                fileChooser.setInitialFileName(initialName);
            }
            File selectedFile = fileChooser.showSaveDialog(owner);
            if (selectedFile != null) {
                destinationField.setText(selectedFile.getAbsolutePath());
            }
        });
        HBox destinationBox = new HBox(8.0, destinationField, browseButton);
        HBox.setHgrow(destinationField, Priority.ALWAYS);

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("dialog-grid");
        gridPane.setHgap(12.0);
        gridPane.setVgap(12.0);
        gridPane.setPadding(new Insets(16.0));
        gridPane.addRow(0, new Label("Archivo MP4"), destinationBox);
        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(buttonType -> buttonType == exportButton
                ? new ExportVideoDialogResult(destinationField.getText())
                : null);

        return dialog.showAndWait();
    }

    private File existingParent(String pathText) {
        if (pathText == null || pathText.isBlank()) {
            return null;
        }
        Path path = Path.of(pathText);
        Path parent = path.getParent();
        if (parent == null || !java.nio.file.Files.isDirectory(parent)) {
            return null;
        }
        return parent.toFile();
    }

    private String fileName(String pathText) {
        if (pathText == null || pathText.isBlank()) {
            return "";
        }
        Path path = Path.of(pathText);
        return path.getFileName() == null ? "" : path.getFileName().toString();
    }
}
