package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.domain.camera.CameraState;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

/**
 * Lightweight presentation model for bookmark thumbnails shown in the sidebar.
 */
final class BookmarkSidebarItem {

    private final String bookmarkId;
    private final CameraState cameraState;
    private final ObjectProperty<Image> thumbnail = new SimpleObjectProperty<>();

    BookmarkSidebarItem(String bookmarkId, CameraState cameraState) {
        this.bookmarkId = bookmarkId;
        this.cameraState = cameraState;
    }

    String bookmarkId() {
        return bookmarkId;
    }

    CameraState cameraState() {
        return cameraState;
    }

    ObjectProperty<Image> thumbnailProperty() {
        return thumbnail;
    }

    void setThumbnail(Image image) {
        thumbnail.set(image);
    }
}
