package com.marcos.fractalstudio.domain.validation;

import com.marcos.fractalstudio.domain.exceptions.TimelineConsistencyException;
import com.marcos.fractalstudio.domain.timeline.Keyframe;
import com.marcos.fractalstudio.domain.timeline.Timeline;

import java.util.List;

/**
 * Enforces structural ordering rules for the animation timeline.
 *
 * <p>The current timeline model assumes strict monotonic growth in keyframe time positions. If two
 * adjacent keyframes collide or go backwards, interpolation semantics become ambiguous and must be
 * rejected as a domain error.
 */
public final class TimelineConsistencyValidator {

    /**
     * Validates that keyframes are strictly ordered by time.
     *
     * @param timeline timeline to validate
     * @throws TimelineConsistencyException when time ordering is not strictly increasing
     */
    public void validate(Timeline timeline) {
        List<Keyframe> keyframes = timeline.keyframes();
        for (int index = 0; index < keyframes.size() - 1; index++) {
            if (keyframes.get(index).timePosition().compareTo(keyframes.get(index + 1).timePosition()) >= 0) {
                throw new TimelineConsistencyException("Timeline keyframes must be strictly ordered by time.");
            }
        }
    }
}
