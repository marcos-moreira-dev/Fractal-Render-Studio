package com.marcos.fractalstudio.presentation.dialogs;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Minimal dialog for renaming a timeline keyframe.
 */
public final class KeyframeLabelDialog {

    public Optional<String> show(Window owner, String currentLabel) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Keyframe");
        dialog.setHeaderText("Renombrar keyframe");
        if (owner != null) {
            dialog.initOwner(owner);
        }
        DialogStyler.apply(dialog);

        ButtonType saveButton = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField labelField = new TextField(currentLabel);
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("dialog-grid");
        gridPane.setHgap(12.0);
        gridPane.setVgap(12.0);
        gridPane.addRow(0, new Label("Nombre"), labelField);
        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(buttonType -> buttonType == saveButton ? labelField.getText() : null);
        return dialog.showAndWait();
    }
}
