package com.marcos.fractalstudio.presentation.shell;

/**
 * Semantic sidebar node used by the project tree.
 *
 * @param kind node category used for interaction behavior
 * @param label user-facing text
 * @param referenceId optional domain identifier for actionable nodes
 */
record SidebarTreeNode(
        Kind kind,
        String label,
        String referenceId
) {

    /**
     * Node categories available in the project sidebar.
     */
    enum Kind {
        ROOT,
        SECTION,
        KEYFRAME,
        BOOKMARK,
        INFO
    }

    static SidebarTreeNode root(String label) {
        return new SidebarTreeNode(Kind.ROOT, label, null);
    }

    static SidebarTreeNode section(String label) {
        return new SidebarTreeNode(Kind.SECTION, label, null);
    }

    static SidebarTreeNode info(String label) {
        return new SidebarTreeNode(Kind.INFO, label, null);
    }

    static SidebarTreeNode keyframe(String label, String keyframeId) {
        return new SidebarTreeNode(Kind.KEYFRAME, label, keyframeId);
    }

    static SidebarTreeNode bookmark(String label, String bookmarkId) {
        return new SidebarTreeNode(Kind.BOOKMARK, label, bookmarkId);
    }

    @Override
    public String toString() {
        return label;
    }
}
