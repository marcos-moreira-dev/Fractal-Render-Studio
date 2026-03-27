package com.marcos.fractalstudio.application.timeline;

import com.marcos.fractalstudio.application.dto.KeyframeDto;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.timeline.Keyframe;
import com.marcos.fractalstudio.domain.timeline.KeyframeId;
import com.marcos.fractalstudio.domain.timeline.TimePosition;

public final class AddKeyframeUseCase {

    public Project add(Project project, CameraState cameraState) {
        TimePosition position = project.timeline().nextSuggestedPosition(project.settings().keyframeStepSeconds());
        Keyframe keyframe = new Keyframe(
                KeyframeId.create(),
                position,
                cameraState,
                "KF-" + (project.timeline().keyframes().size() + 1)
        );
        return project.withTimeline(project.timeline().addKeyframe(keyframe));
    }

    public KeyframeDto toDto(Keyframe keyframe) {
        return new KeyframeDto(
                keyframe.id().value(),
                keyframe.label(),
                keyframe.timePosition().seconds(),
                keyframe.cameraState().center().x(),
                keyframe.cameraState().center().y(),
                keyframe.cameraState().zoomLevel().value()
        );
    }
}
