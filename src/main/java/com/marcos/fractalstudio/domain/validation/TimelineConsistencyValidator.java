package com.marcos.fractalstudio.domain.validation;

import com.marcos.fractalstudio.domain.timeline.Keyframe;
import com.marcos.fractalstudio.domain.timeline.Timeline;

import java.util.List;

public final class TimelineConsistencyValidator {

    public void validate(Timeline timeline) {
        List<Keyframe> keyframes = timeline.keyframes();
        for (int index = 0; index < keyframes.size() - 1; index++) {
            if (keyframes.get(index).timePosition().compareTo(keyframes.get(index + 1).timePosition()) >= 0) {
                throw new IllegalStateException("Timeline keyframes must be strictly ordered by time.");
            }
        }
    }
}
