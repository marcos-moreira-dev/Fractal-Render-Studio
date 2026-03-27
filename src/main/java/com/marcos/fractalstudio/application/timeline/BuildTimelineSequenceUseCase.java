package com.marcos.fractalstudio.application.timeline;

import com.marcos.fractalstudio.domain.camera.CameraPathInterpolator;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;
import com.marcos.fractalstudio.domain.render.FrameDescriptor;
import com.marcos.fractalstudio.domain.render.FrameIndex;
import com.marcos.fractalstudio.domain.render.RenderPreset;
import com.marcos.fractalstudio.domain.timeline.TimePosition;
import com.marcos.fractalstudio.application.render.RenderPresetProfiles;

import java.util.ArrayList;
import java.util.List;

public final class BuildTimelineSequenceUseCase {

    public List<FrameDescriptor> build(
            Project project,
            CameraState fallbackCamera,
            int totalFrames,
            double framesPerSecond,
            RenderPreset renderPreset
    ) {
        List<FrameDescriptor> frameDescriptors = new ArrayList<>(totalFrames);
        double totalDurationSeconds = Math.max(0.0, (totalFrames - 1) / framesPerSecond);

        for (int index = 0; index < totalFrames; index++) {
            double seconds = index / framesPerSecond;
            TimePosition timePosition = new TimePosition(seconds);
            frameDescriptors.add(new FrameDescriptor(
                    new FrameIndex(index),
                    timePosition,
                    resolveCamera(project, fallbackCamera, timePosition, totalDurationSeconds),
                    project.fractalFormula(),
                    RenderPresetProfiles.apply(project.renderProfile(), renderPreset),
                    project.colorProfile()
            ));
        }

        return frameDescriptors;
    }

    private CameraState resolveCamera(
            Project project,
            CameraState fallbackCamera,
            TimePosition timePosition,
            double totalDurationSeconds
    ) {
        if (project.timeline().keyframes().size() >= 2) {
            return project.timeline().resolveCameraAt(timePosition, fallbackCamera);
        }
        if (project.bookmarks().size() >= 2) {
            return resolveBookmarkCamera(project.bookmarks(), timePosition.seconds(), totalDurationSeconds);
        }
        if (project.timeline().keyframes().size() == 1) {
            return project.timeline().resolveCameraAt(timePosition, fallbackCamera);
        }
        if (project.bookmarks().size() == 1) {
            return project.bookmarks().getFirst().cameraState();
        }
        return fallbackCamera;
    }

    private CameraState resolveBookmarkCamera(List<ProjectBookmark> bookmarks, double seconds, double totalDurationSeconds) {
        if (bookmarks.size() == 1 || totalDurationSeconds <= 0.0) {
            return bookmarks.getFirst().cameraState();
        }
        if (seconds <= 0.0) {
            return bookmarks.getFirst().cameraState();
        }
        if (seconds >= totalDurationSeconds) {
            return bookmarks.getLast().cameraState();
        }

        double segmentDuration = totalDurationSeconds / (bookmarks.size() - 1);
        if (segmentDuration <= 0.0) {
            return bookmarks.getFirst().cameraState();
        }

        int segmentIndex = Math.min(bookmarks.size() - 2, (int) Math.floor(seconds / segmentDuration));
        ProjectBookmark start = bookmarks.get(segmentIndex);
        ProjectBookmark end = bookmarks.get(segmentIndex + 1);
        double segmentStartSeconds = segmentIndex * segmentDuration;
        double ratio = (seconds - segmentStartSeconds) / segmentDuration;
        return CameraPathInterpolator.interpolate(start.cameraState(), end.cameraState(), ratio);
    }
}
