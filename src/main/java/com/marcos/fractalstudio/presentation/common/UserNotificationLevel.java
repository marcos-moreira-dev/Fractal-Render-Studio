package com.marcos.fractalstudio.presentation.common;

/**
 * Semantic severity used by the desktop UI to decide how a user-facing
 * notification should be rendered.
 *
 * <p>The enum is presentation-friendly but UI-toolkit-agnostic, so view models
 * can publish notifications without depending on JavaFX alert classes.
 */
public enum UserNotificationLevel {
    INFO,
    WARNING,
    ERROR
}
