package com.marcos.fractalstudio.presentation.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;

/**
 * Applies the shared studio stylesheet to transient dialogs and alerts so
 * they match the desktop shell theme.
 */
public final class DialogStyler {

    private static final String STUDIO_STYLESHEET =
            DialogStyler.class.getResource("/assets/styles/studio.css").toExternalForm();

    private DialogStyler() {
    }

    public static void apply(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(STUDIO_STYLESHEET);
        dialog.getDialogPane().getStyleClass().add("studio-dialog");
    }

    public static void apply(Alert alert) {
        apply((Dialog<?>) alert);
    }
}
