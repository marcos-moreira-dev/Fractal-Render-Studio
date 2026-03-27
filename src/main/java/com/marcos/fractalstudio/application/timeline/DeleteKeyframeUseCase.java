package com.marcos.fractalstudio.application.timeline;

import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.timeline.KeyframeId;

/**
 * Removes a keyframe from the project timeline by identifier.
 */
public final class DeleteKeyframeUseCase {

    public Project delete(Project project, String keyframeId) {
        return project.withTimeline(project.timeline().removeKeyframe(new KeyframeId(keyframeId)));
    }
}
