package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;

import java.util.ArrayList;

/**
 * Renames a bookmark while preserving its mathematical camera reference.
 */
public final class RenameBookmarkUseCase {

    public Project rename(Project project, String bookmarkId, String newLabel) {
        ArrayList<ProjectBookmark> bookmarks = new ArrayList<>(project.bookmarks());
        for (int index = 0; index < bookmarks.size(); index++) {
            ProjectBookmark bookmark = bookmarks.get(index);
            if (bookmark.id().value().equals(bookmarkId)) {
                bookmarks.set(index, new ProjectBookmark(bookmark.id(), newLabel, bookmark.cameraState()));
                break;
            }
        }
        return project.withBookmarks(bookmarks);
    }
}
