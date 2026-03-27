package com.marcos.fractalstudio.presentation.timeline;

import com.marcos.fractalstudio.application.dto.KeyframeDto;
import com.marcos.fractalstudio.domain.camera.CameraState;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public final class KeyframeTimelineItem {

    private final KeyframeDto keyframe;
    private final CameraState cameraState;
    private final ObjectProperty<Image> thumbnail = new SimpleObjectProperty<>();
    private final StringProperty thumbnailStatus = new SimpleStringProperty("Generando miniatura...");

    public KeyframeTimelineItem(KeyframeDto keyframe, CameraState cameraState) {
        this.keyframe = keyframe;
        this.cameraState = cameraState;
    }

    public KeyframeDto keyframe() {
        return keyframe;
    }

    public CameraState cameraState() {
        return cameraState;
    }

    public ObjectProperty<Image> thumbnailProperty() {
        return thumbnail;
    }

    public StringProperty thumbnailStatusProperty() {
        return thumbnailStatus;
    }

    public void setThumbnail(Image image) {
        thumbnail.set(image);
        thumbnailStatus.set("Miniatura lista");
    }

    public void setThumbnailStatus(String status) {
        thumbnailStatus.set(status);
    }
}
