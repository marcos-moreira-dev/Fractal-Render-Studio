package com.marcos.fractalstudio.presentation.common;

import javafx.application.Platform;

public final class UiThreadExecutor {

    public void execute(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }
        Platform.runLater(runnable);
    }
}
