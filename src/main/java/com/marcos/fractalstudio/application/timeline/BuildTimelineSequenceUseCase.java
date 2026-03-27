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

/**
 * Derives the full ordered sequence of frame descriptors that a render job must
 * process for a given project, duration and frame rate.
 *
 * <p>This use case is where the product-level idea of "points become an
 * animation" is turned into a concrete list of per-frame mathematical states.
 * It can resolve camera motion from:
 *
 * <ul>
 *   <li>a timeline with two or more keyframes</li>
 *   <li>a fallback bookmark path when there is no rich timeline yet</li>
 *   <li>a single static point or keyframe when the output should remain fixed</li>
 * </ul>
 */
public final class BuildTimelineSequenceUseCase {

    /**
     * Builds the frame-by-frame render plan for the requested animation.
     *
     * @param project project aggregate that contributes formula, color and path
     * @param fallbackCamera camera used when the project has no usable path
     * @param totalFrames number of frames expected in the final output
     * @param framesPerSecond temporal sampling rate of the output video
     * @param renderPreset preset to apply on top of the project's base profile
     * @return ordered frame descriptors ready for rendering infrastructure
     */
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

    /**
     * Resolves which camera state should be used for a specific time position.
     *
     * <p>The order of precedence is intentional:
     *
     * <ol>
     *   <li>rich timeline interpolation when the project has at least two keyframes</li>
     *   <li>bookmark interpolation when the user only saved points</li>
     *   <li>a single keyframe or bookmark as static output</li>
     *   <li>the supplied fallback camera</li>
     * </ol>
     */
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

    /**
     * Interpolates camera motion across saved project bookmarks.
     *
     * <p>This path keeps the software useful even when the user has not built a
     * dedicated timeline yet. In practice, it lets saved points behave as a
     * lightweight animation path and prevents the render pipeline from
     * degenerating into a single repeated frame.
     */
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
