package com.marcos.fractalstudio.domain.timeline;

import com.marcos.fractalstudio.domain.camera.CameraPathInterpolator;
import com.marcos.fractalstudio.domain.camera.CameraState;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class Timeline {

    private final List<Keyframe> keyframes;

    public Timeline() {
        this(List.of());
    }

    public Timeline(List<Keyframe> keyframes) {
        Objects.requireNonNull(keyframes, "Keyframes are required.");
        List<Keyframe> sortedKeyframes = keyframes.stream()
                .sorted(Comparator.comparing(Keyframe::timePosition))
                .toList();
        validateUniqueTimePositions(sortedKeyframes);
        this.keyframes = sortedKeyframes;
    }

    public Timeline addKeyframe(Keyframe keyframe) {
        List<Keyframe> updatedKeyframes = new java.util.ArrayList<>(keyframes);
        updatedKeyframes.add(keyframe);
        return new Timeline(updatedKeyframes);
    }

    public List<Keyframe> keyframes() {
        return keyframes;
    }

    public TimePosition nextSuggestedPosition() {
        return nextSuggestedPosition(2.0);
    }

    public TimePosition nextSuggestedPosition(double keyframeStepSeconds) {
        if (keyframes.isEmpty()) {
            return new TimePosition(0.0);
        }
        return keyframes.getLast().timePosition().addSeconds(keyframeStepSeconds);
    }

    public CameraState resolveCameraAt(TimePosition timePosition, CameraState fallbackCamera) {
        if (keyframes.isEmpty()) {
            return fallbackCamera;
        }
        if (keyframes.size() == 1) {
            return keyframes.getFirst().cameraState();
        }

        Keyframe first = keyframes.getFirst();
        Keyframe last = keyframes.getLast();
        if (timePosition.compareTo(first.timePosition()) <= 0) {
            return first.cameraState();
        }
        if (timePosition.compareTo(last.timePosition()) >= 0) {
            return last.cameraState();
        }

        for (int index = 0; index < keyframes.size() - 1; index++) {
            Keyframe start = keyframes.get(index);
            Keyframe end = keyframes.get(index + 1);
            if (timePosition.compareTo(start.timePosition()) >= 0 && timePosition.compareTo(end.timePosition()) <= 0) {
                double range = end.timePosition().seconds() - start.timePosition().seconds();
                double ratio = range == 0.0 ? 0.0 : (timePosition.seconds() - start.timePosition().seconds()) / range;
                return CameraPathInterpolator.interpolate(start.cameraState(), end.cameraState(), ratio);
            }
        }

        return fallbackCamera;
    }

    public Timeline removeKeyframe(KeyframeId keyframeId) {
        return new Timeline(keyframes.stream()
                .filter(keyframe -> !keyframe.id().equals(keyframeId))
                .toList());
    }

    private void validateUniqueTimePositions(List<Keyframe> sortedKeyframes) {
        for (int index = 0; index < sortedKeyframes.size() - 1; index++) {
            TimePosition current = sortedKeyframes.get(index).timePosition();
            TimePosition next = sortedKeyframes.get(index + 1).timePosition();
            if (current.compareTo(next) == 0) {
                throw new IllegalArgumentException("Timeline cannot contain two keyframes at the same time position.");
            }
        }
    }
}
