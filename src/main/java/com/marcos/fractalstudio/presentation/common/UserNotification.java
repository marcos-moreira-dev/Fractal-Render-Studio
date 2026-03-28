package com.marcos.fractalstudio.presentation.common;

/**
 * Immutable description of a notification that the shell should present to the
 * user.
 *
 * <p>This record works as a transient bridge between a view model and the
 * window shell: the view model emits semantic information, while the shell
 * decides whether it becomes an alert, toast or any other visual element.
 *
 * @param level semantic severity of the message
 * @param title short title suitable for a dialog title bar
 * @param header short contextual headline
 * @param message user-facing explanatory body
 */
public record UserNotification(
        UserNotificationLevel level,
        String title,
        String header,
        String message
) {
}
