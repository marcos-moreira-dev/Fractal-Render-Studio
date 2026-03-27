package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Reorders bookmarks inside the project aggregate without altering their ids.
 */
public final class MoveBookmarkUseCase {

    public Project move(Project project, String bookmarkId, int direction) {
        ArrayList<ProjectBookmark> bookmarks = new ArrayList<>(project.bookmarks());
        for (int index = 0; index < bookmarks.size(); index++) {
            if (!bookmarks.get(index).id().value().equals(bookmarkId)) {
                continue;
            }
            int targetIndex = Math.max(0, Math.min(bookmarks.size() - 1, index + direction));
            if (targetIndex != index) {
                Collections.swap(bookmarks, index, targetIndex);
            }
            break;
        }
        return project.withBookmarks(bookmarks);
    }
}
