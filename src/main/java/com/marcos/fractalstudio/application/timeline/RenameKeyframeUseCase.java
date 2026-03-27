package com.marcos.fractalstudio.application.timeline;

import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.timeline.Keyframe;
import com.marcos.fractalstudio.domain.timeline.Timeline;

import java.util.ArrayList;

/**
 * Renames a keyframe while preserving its camera state and time position.
 */
public final class RenameKeyframeUseCase {

    public Project rename(Project project, String keyframeId, String newLabel) {
        ArrayList<Keyframe> keyframes = new ArrayList<>(project.timeline().keyframes());
        for (int index = 0; index < keyframes.size(); index++) {
            Keyframe keyframe = keyframes.get(index);
            if (keyframe.id().value().equals(keyframeId)) {
                keyframes.set(index, new Keyframe(
                        keyframe.id(),
                        keyframe.timePosition(),
                        keyframe.cameraState(),
                        newLabel
                ));
                break;
            }
        }
        return project.withTimeline(new Timeline(keyframes));
    }
}
