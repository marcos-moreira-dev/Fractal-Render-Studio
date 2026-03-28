package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.application.dto.KeyframeDto;
import com.marcos.fractalstudio.application.project.ProjectFacade;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;
import com.marcos.fractalstudio.domain.project.ProjectBookmarkId;
import com.marcos.fractalstudio.domain.timeline.Keyframe;
import com.marcos.fractalstudio.domain.timeline.KeyframeId;
import com.marcos.fractalstudio.domain.timeline.TimePosition;
import com.marcos.fractalstudio.domain.timeline.Timeline;
import com.marcos.fractalstudio.presentation.timeline.KeyframeTimelineItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the "points as timeline" rule used by the desktop studio.
 *
 * <p>The product currently presents saved points, bookmarks and timeline entries as almost the same
 * user concept. Keeping that synchronization logic inside the shell view model made it difficult to
 * reason about edits and navigation. This coordinator isolates that policy: it owns the selected point
 * index, keeps project bookmarks/timeline aligned and returns explicit outcomes for the UI to apply.
 */
final class StudioPointsTimelineCoordinator {

    private Project project;
    private int selectedBookmarkIndex = -1;

    Project setProject(Project project, boolean resetSelection) {
        this.project = synchronize(project);
        if (resetSelection) {
            selectedBookmarkIndex = -1;
        } else {
            clampSelection();
        }
        return this.project;
    }

    Project project() {
        return project;
    }

    PointMutation addPoint(ProjectFacade projectFacade, CameraState cameraState) {
        String pointLabel = nextPointLabel();
        project = projectFacade.addBookmark(project, cameraState);
        ProjectBookmark bookmark = project.bookmarks().getLast();
        project = projectFacade.renameBookmark(project, bookmark.id().value(), pointLabel);
        project = projectFacade.createKeyframeFromBookmark(project, bookmark.id().value());
        String keyframeId = project.timeline().keyframes().getLast().id().value();
        project = projectFacade.renameKeyframe(project, keyframeId, pointLabel);
        project = synchronize(project);
        selectedBookmarkIndex = project.bookmarks().size() - 1;
        return PointMutation.changed(project, "Punto guardado: " + pointLabel, false);
    }

    PointMutation renameKeyframe(ProjectFacade projectFacade, String keyframeId, String newLabel) {
        String normalizedLabel = normalizeLabel(newLabel);
        if (normalizedLabel == null) {
            return PointMutation.noChange(project);
        }
        String currentLabel = currentKeyframeLabel(keyframeId);
        String linkedBookmarkId = findLinkedBookmarkIdForKeyframe(keyframeId);
        project = projectFacade.renameKeyframe(project, keyframeId, normalizedLabel);
        if (linkedBookmarkId != null) {
            project = projectFacade.renameBookmark(project, linkedBookmarkId, normalizedLabel);
        }
        project = synchronize(project);
        return PointMutation.changed(project, "Punto renombrado: " + currentLabel + " -> " + normalizedLabel, false);
    }

    PointMutation deleteKeyframe(ProjectFacade projectFacade, String keyframeId) {
        int keyframeCountBefore = project.timeline().keyframes().size();
        String linkedBookmarkId = findLinkedBookmarkIdForKeyframe(keyframeId);
        project = projectFacade.deleteKeyframe(project, keyframeId);
        if (project.timeline().keyframes().size() == keyframeCountBefore) {
            return PointMutation.noChange(project);
        }
        if (linkedBookmarkId != null) {
            project = projectFacade.deleteBookmark(project, linkedBookmarkId);
        }
        project = synchronize(project);
        clampSelection();
        return PointMutation.changed(project, "Punto eliminado", true);
    }

    PointMutation deleteBookmark(ProjectFacade projectFacade, String bookmarkId) {
        int bookmarkCountBefore = project.bookmarks().size();
        String linkedKeyframeId = findLinkedKeyframeIdForBookmark(bookmarkId);
        project = projectFacade.deleteBookmark(project, bookmarkId);
        if (project.bookmarks().size() == bookmarkCountBefore) {
            return PointMutation.noChange(project);
        }
        if (linkedKeyframeId != null) {
            project = projectFacade.deleteKeyframe(project, linkedKeyframeId);
        }
        clampSelection();
        project = synchronize(project);
        clampSelection();
        return PointMutation.changed(project, "Punto eliminado", true);
    }

    PointMutation renameBookmark(ProjectFacade projectFacade, String bookmarkId, String newLabel) {
        String normalizedLabel = normalizeLabel(newLabel);
        if (normalizedLabel == null) {
            return PointMutation.noChange(project);
        }
        String currentLabel = currentBookmarkLabel(bookmarkId);
        String linkedKeyframeId = findLinkedKeyframeIdForBookmark(bookmarkId);
        project = projectFacade.renameBookmark(project, bookmarkId, normalizedLabel);
        if (linkedKeyframeId != null) {
            project = projectFacade.renameKeyframe(project, linkedKeyframeId, normalizedLabel);
        }
        project = synchronize(project);
        return PointMutation.changed(project, "Punto renombrado: " + currentLabel + " -> " + normalizedLabel, false);
    }

    PointMutation moveBookmark(ProjectFacade projectFacade, String bookmarkId, int direction) {
        if (direction == 0) {
            return PointMutation.noChange(project);
        }
        int previousIndex = indexOfBookmark(bookmarkId);
        if (previousIndex < 0) {
            return PointMutation.noChange(project);
        }
        int targetIndex = Math.max(0, Math.min(project.bookmarks().size() - 1, previousIndex + direction));
        if (targetIndex == previousIndex) {
            return PointMutation.noChange(project);
        }
        project = projectFacade.moveBookmark(project, bookmarkId, direction);
        selectedBookmarkIndex = targetIndex;
        project = synchronize(project);
        return PointMutation.changed(project, "Punto reordenado", false);
    }

    PointMutation createKeyframeFromBookmark(ProjectFacade projectFacade, String bookmarkId) {
        int keyframeCountBefore = project.timeline().keyframes().size();
        project = projectFacade.createKeyframeFromBookmark(project, bookmarkId);
        if (project.timeline().keyframes().size() == keyframeCountBefore) {
            return PointMutation.noChange(project);
        }
        String bookmarkLabel = project.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .map(ProjectBookmark::label)
                .findFirst()
                .orElse("Punto");
        String createdKeyframeId = project.timeline().keyframes().getLast().id().value();
        project = projectFacade.renameKeyframe(project, createdKeyframeId, bookmarkLabel);
        project = synchronize(project);
        return PointMutation.changed(project, "Punto agregado al timeline", false);
    }

    FocusChange jumpToBookmark(int direction) {
        if (project.bookmarks().isEmpty()) {
            return FocusChange.notFound("No hay puntos guardados");
        }
        if (selectedBookmarkIndex < 0) {
            selectedBookmarkIndex = direction > 0 ? 0 : project.bookmarks().size() - 1;
        } else {
            selectedBookmarkIndex = Math.floorMod(selectedBookmarkIndex + direction, project.bookmarks().size());
        }
        ProjectBookmark bookmark = project.bookmarks().get(selectedBookmarkIndex);
        return FocusChange.found(bookmark.cameraState(), "Punto cargado: " + bookmark.label(), true);
    }

    FocusChange focusKeyframe(String keyframeId) {
        return project.timeline().keyframes().stream()
                .filter(keyframe -> keyframe.id().value().equals(keyframeId))
                .findFirst()
                .map(keyframe -> FocusChange.found(keyframe.cameraState(), "Keyframe cargado: " + keyframe.label(), true))
                .orElseGet(FocusChange::notFound);
    }

    FocusChange focusBookmark(String bookmarkId) {
        return project.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .findFirst()
                .map(bookmark -> {
                    selectedBookmarkIndex = project.bookmarks().indexOf(bookmark);
                    return FocusChange.found(bookmark.cameraState(), "Punto cargado: " + bookmark.label(), true);
                })
                .orElseGet(FocusChange::notFound);
    }

    String currentBookmarkLabel(String bookmarkId) {
        return project.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .map(ProjectBookmark::label)
                .findFirst()
                .orElse("");
    }

    String currentKeyframeLabel(String keyframeId) {
        return project.timeline().keyframes().stream()
                .filter(keyframe -> keyframe.id().value().equals(keyframeId))
                .map(Keyframe::label)
                .findFirst()
                .orElse("");
    }

    List<KeyframeTimelineItem> buildTimelineItems(List<KeyframeDto> keyframeDtos) {
        List<Keyframe> domainKeyframes = project.timeline().keyframes();
        ArrayList<KeyframeTimelineItem> items = new ArrayList<>(keyframeDtos.size());
        for (int index = 0; index < keyframeDtos.size() && index < domainKeyframes.size(); index++) {
            items.add(new KeyframeTimelineItem(keyframeDtos.get(index), domainKeyframes.get(index).cameraState()));
        }
        return items;
    }

    private String normalizeLabel(String label) {
        String normalizedLabel = label == null ? "" : label.trim();
        return normalizedLabel.isBlank() ? null : normalizedLabel;
    }

    private void clampSelection() {
        selectedBookmarkIndex = Math.min(selectedBookmarkIndex, project.bookmarks().size() - 1);
        if (project.bookmarks().isEmpty()) {
            selectedBookmarkIndex = -1;
        }
    }

    private String nextPointLabel() {
        int nextIndex = Math.max(project.bookmarks().size(), project.timeline().keyframes().size()) + 1;
        return "P-" + nextIndex;
    }

    private int indexOfBookmark(String bookmarkId) {
        for (int index = 0; index < project.bookmarks().size(); index++) {
            if (project.bookmarks().get(index).id().value().equals(bookmarkId)) {
                return index;
            }
        }
        return -1;
    }

    private String findLinkedBookmarkIdForKeyframe(String keyframeId) {
        return project.timeline().keyframes().stream()
                .filter(keyframe -> keyframe.id().value().equals(keyframeId))
                .findFirst()
                .flatMap(keyframe -> project.bookmarks().stream()
                        .filter(bookmark -> bookmark.label().equals(keyframe.label()))
                        .filter(bookmark -> sameCameraState(bookmark.cameraState(), keyframe.cameraState()))
                        .map(bookmark -> bookmark.id().value())
                        .findFirst())
                .orElse(null);
    }

    private String findLinkedKeyframeIdForBookmark(String bookmarkId) {
        return project.bookmarks().stream()
                .filter(bookmark -> bookmark.id().value().equals(bookmarkId))
                .findFirst()
                .flatMap(bookmark -> project.timeline().keyframes().stream()
                        .filter(keyframe -> keyframe.label().equals(bookmark.label()))
                        .filter(keyframe -> sameCameraState(bookmark.cameraState(), keyframe.cameraState()))
                        .map(keyframe -> keyframe.id().value())
                        .findFirst())
                .orElse(null);
    }

    private boolean sameCameraState(CameraState left, CameraState right) {
        return left.center().xDecimal().compareTo(right.center().xDecimal()) == 0
                && left.center().yDecimal().compareTo(right.center().yDecimal()) == 0
                && left.zoomLevel().valueDecimal().compareTo(right.zoomLevel().valueDecimal()) == 0;
    }

    private Project synchronize(Project project) {
        if (!project.bookmarks().isEmpty()) {
            return project.withTimeline(rebuildTimelineFromBookmarks(project.bookmarks(), project.settings().keyframeStepSeconds()));
        }
        if (!project.timeline().keyframes().isEmpty()) {
            return project.withBookmarks(rebuildBookmarksFromTimeline(project.timeline().keyframes()));
        }
        return project;
    }

    private Timeline rebuildTimelineFromBookmarks(List<ProjectBookmark> bookmarks, double keyframeStepSeconds) {
        ArrayList<Keyframe> keyframeList = new ArrayList<>(bookmarks.size());
        for (int index = 0; index < bookmarks.size(); index++) {
            ProjectBookmark bookmark = bookmarks.get(index);
            keyframeList.add(new Keyframe(
                    KeyframeId.create(),
                    new TimePosition(index * keyframeStepSeconds),
                    bookmark.cameraState(),
                    bookmark.label()
            ));
        }
        return new Timeline(keyframeList);
    }

    private List<ProjectBookmark> rebuildBookmarksFromTimeline(List<Keyframe> keyframeList) {
        ArrayList<ProjectBookmark> bookmarks = new ArrayList<>(keyframeList.size());
        for (Keyframe keyframe : keyframeList) {
            bookmarks.add(new ProjectBookmark(
                    ProjectBookmarkId.create(),
                    keyframe.label(),
                    keyframe.cameraState()
            ));
        }
        return bookmarks;
    }

    record PointMutation(Project project, boolean changed, String metricMessage, boolean requestAutoPreview) {
        static PointMutation changed(Project project, String metricMessage, boolean requestAutoPreview) {
            return new PointMutation(project, true, metricMessage, requestAutoPreview);
        }

        static PointMutation noChange(Project project) {
            return new PointMutation(project, false, null, false);
        }
    }

    record FocusChange(CameraState cameraState, String metricMessage, boolean requestAutoPreview, boolean found) {
        static FocusChange found(CameraState cameraState, String metricMessage, boolean requestAutoPreview) {
            return new FocusChange(cameraState, metricMessage, requestAutoPreview, true);
        }

        static FocusChange notFound() {
            return new FocusChange(null, null, false, false);
        }

        static FocusChange notFound(String metricMessage) {
            return new FocusChange(null, metricMessage, false, false);
        }
    }
}
