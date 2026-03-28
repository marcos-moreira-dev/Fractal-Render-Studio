package com.marcos.fractalstudio.presentation.dialogs;

import com.marcos.fractalstudio.presentation.common.UserNotification;
import com.marcos.fractalstudio.presentation.common.UserNotificationLevel;
import javafx.scene.control.Alert;
import javafx.stage.Window;

/**
 * Presents semantic user notifications as styled JavaFX alerts.
 *
 * <p>The class centralizes alert creation so the desktop shell can keep a
 * consistent visual language for warnings and failures instead of scattering
 * hand-written alert configuration across multiple views.
 */
public final class ExceptionAlertPresenter {

    /**
     * Shows the supplied notification using the shared dialog styling.
     *
     * @param owner owner window, when available
     * @param notification semantic notification to present
     */
    public void show(Window owner, UserNotification notification) {
        Alert alert = new Alert(resolveAlertType(notification.level()));
        alert.setTitle(notification.title());
        alert.setHeaderText(notification.header());
        alert.setContentText(notification.message());
        if (owner != null) {
            alert.initOwner(owner);
        }
        DialogStyler.apply(alert);
        alert.showAndWait();
    }

    private Alert.AlertType resolveAlertType(UserNotificationLevel level) {
        return switch (level) {
            case INFO -> Alert.AlertType.INFORMATION;
            case WARNING -> Alert.AlertType.WARNING;
            case ERROR -> Alert.AlertType.ERROR;
        };
    }
}
