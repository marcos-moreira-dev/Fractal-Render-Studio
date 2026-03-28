package com.marcos.fractalstudio.presentation.shell;

/**
 * Identifies which temporary workspace drawer is visible beneath the main viewport.
 *
 * <p>The desktop shell currently exposes one drawer for saved points/timeline editing and another
 * for render job supervision. Keeping the tab as a dedicated concept avoids leaking raw booleans
 * throughout the presentation layer.
 */
public enum WorkspaceDrawerTab {
    POINTS,
    RENDER_QUEUE
}
