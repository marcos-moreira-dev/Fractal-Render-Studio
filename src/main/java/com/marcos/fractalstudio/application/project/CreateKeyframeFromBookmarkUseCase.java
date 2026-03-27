package com.marcos.fractalstudio.application.project;

import com.marcos.fractalstudio.application.timeline.AddKeyframeUseCase;
import com.marcos.fractalstudio.domain.project.Project;

/**
 * Converts a saved bookmark camera into a timeline keyframe without removing
 * the original bookmark.
 */
public final class CreateKeyframeFromBookmarkUseCase {

    private final AddKeyframeUseCase addKeyframeUseCase;

    public CreateKeyframeFromBookmarkUseCase(AddKeyframeUseCase addKeyframeUseCase) {
        this.addKeyframeUseCase = addKeyframeUseCase;
    }

    public Project create(Project project, String bookmarkId) {
        return project.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .findFirst()
                .map(bookmark -> addKeyframeUseCase.add(project, bookmark.cameraState()))
                .orElse(project);
    }
}
