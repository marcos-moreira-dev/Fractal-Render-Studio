package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;
import com.marcos.fractalstudio.domain.project.ProjectBookmarkId;

import java.util.ArrayList;

public final class AddBookmarkUseCase {

    public Project add(Project project, CameraState cameraState) {
        ArrayList<ProjectBookmark> bookmarks = new ArrayList<>(project.bookmarks());
        bookmarks.add(new ProjectBookmark(
                ProjectBookmarkId.create(),
                "BM-" + (bookmarks.size() + 1),
                cameraState
        ));
        return project.withBookmarks(bookmarks);
    }
}
