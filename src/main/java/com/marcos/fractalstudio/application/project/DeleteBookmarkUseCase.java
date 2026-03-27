package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.project.Project;

import java.util.ArrayList;

/**
 * Removes a bookmark from the project aggregate by identifier.
 */
public final class DeleteBookmarkUseCase {

    public Project delete(Project project, String bookmarkId) {
        ArrayList<com.marcos.fractalstudio.domain.project.ProjectBookmark> bookmarks = new ArrayList<>(project.bookmarks());
        bookmarks.removeIf(bookmark -> bookmark.id().value().equals(bookmarkId));
        return project.withBookmarks(bookmarks);
    }
}
