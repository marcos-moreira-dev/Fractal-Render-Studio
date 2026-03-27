package com.marcos.fractalstudio.infrastructure.persistence;

import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.camera.FractalCoordinate;
import com.marcos.fractalstudio.domain.camera.ZoomLevel;
import com.marcos.fractalstudio.domain.color.ColorProfile;
import com.marcos.fractalstudio.domain.color.ColorStop;
import com.marcos.fractalstudio.domain.color.Palette;
import com.marcos.fractalstudio.domain.color.RgbColor;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaFactory;
import com.marcos.fractalstudio.domain.fractal.FractalFormulaType;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.domain.project.ProjectBookmark;
import com.marcos.fractalstudio.domain.project.ProjectBookmarkId;
import com.marcos.fractalstudio.domain.project.ProjectId;
import com.marcos.fractalstudio.domain.project.ProjectMetadata;
import com.marcos.fractalstudio.domain.project.ProjectName;
import com.marcos.fractalstudio.domain.project.ProjectSettings;
import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.RenderPreset;
import com.marcos.fractalstudio.domain.render.RenderProfile;
import com.marcos.fractalstudio.domain.render.RenderQuality;
import com.marcos.fractalstudio.domain.render.Resolution;
import com.marcos.fractalstudio.domain.timeline.Keyframe;
import com.marcos.fractalstudio.domain.timeline.KeyframeId;
import com.marcos.fractalstudio.domain.timeline.TimePosition;
import com.marcos.fractalstudio.domain.timeline.Timeline;

import java.time.Instant;

/**
 * Maps between the domain project aggregate and its JSON persistence snapshot.
 *
 * <p>The mapper protects the domain from serialization concerns and keeps file
 * format evolution localized. It also handles compatibility defaults for older
 * project documents that may lack newer metadata or settings fields.
 */
public final class ProjectSnapshotMapper {

    /**
     * Converts a domain project aggregate into its serializable document form.
     *
     * <p>The resulting document is designed to preserve the mathematical state
     * of the project rather than only its visual result. This is what allows a
     * saved project to be reopened and re-rendered at different qualities later.
     */
    public ProjectDocument toDocument(Project project) {
        return new ProjectDocument(
                project.id().value(),
                project.name().value(),
                FractalFormulaFactory.resolveType(project.fractalFormula()).name(),
                new RenderProfileDocument(
                        project.renderProfile().name(),
                        project.renderProfile().resolution().width(),
                        project.renderProfile().resolution().height(),
                        project.renderProfile().escapeParameters().maxIterations(),
                        project.renderProfile().escapeParameters().escapeRadius(),
                        project.renderProfile().quality().name()
                ),
                new ColorProfileDocument(
                        project.colorProfile().name(),
                        project.colorProfile().palette().colorStops().stream()
                                .map(stop -> new ColorStopDocument(
                                        stop.position(),
                                        stop.color().red(),
                                        stop.color().green(),
                                        stop.color().blue()
                                ))
                                .toList()
                ),
                new ProjectMetadataDocument(
                        project.metadata().createdAt().toString(),
                        project.metadata().updatedAt().toString(),
                        project.metadata().description()
                ),
                new ProjectSettingsDocument(
                        project.settings().defaultFramesPerSecond(),
                        project.settings().defaultRenderFrameCount(),
                        project.settings().keyframeStepSeconds(),
                        project.settings().defaultRenderPreset().name()
                ),
                project.timeline().keyframes().stream()
                        .map(keyframe -> new KeyframeDocument(
                                keyframe.id().value(),
                                keyframe.timePosition().seconds(),
                                keyframe.cameraState().center().xPlainString(),
                                keyframe.cameraState().center().yPlainString(),
                                keyframe.cameraState().zoomLevel().plainString(),
                                keyframe.label()
                        ))
                        .toList(),
                project.bookmarks().stream()
                        .map(bookmark -> new BookmarkDocument(
                                bookmark.id().value(),
                                bookmark.label(),
                                bookmark.cameraState().center().xPlainString(),
                                bookmark.cameraState().center().yPlainString(),
                                bookmark.cameraState().zoomLevel().plainString()
                        ))
                        .toList()
        );
    }

    /**
     * Rebuilds a domain project aggregate from a persisted snapshot.
     *
     * <p>When fields are missing because the file originates from an earlier
     * version of the product, the mapper supplies explicit defaults instead of
     * failing silently.
     */
    public Project toDomain(ProjectDocument projectDocument) {
        Timeline timeline = new Timeline(projectDocument.keyframes().stream()
                .map(keyframe -> new Keyframe(
                        new KeyframeId(keyframe.id()),
                        new TimePosition(keyframe.seconds()),
                        new CameraState(
                                new FractalCoordinate(keyframe.centerX(), keyframe.centerY()),
                                new ZoomLevel(keyframe.zoom())
                        ),
                        keyframe.label()
                ))
                .toList());

        ProjectMetadataDocument metadataDocument = projectDocument.metadata() == null
                ? new ProjectMetadataDocument(Instant.now().toString(), Instant.now().toString(), "Imported project.")
                : projectDocument.metadata();

        ProjectSettingsDocument settingsDocument = projectDocument.settings() == null
                ? new ProjectSettingsDocument(6.0, 24, 2.0, RenderPreset.STANDARD.name())
                : projectDocument.settings();

        java.util.List<BookmarkDocument> bookmarkDocuments = projectDocument.bookmarks() == null
                ? java.util.List.of()
                : projectDocument.bookmarks();

        return new Project(
                new ProjectId(projectDocument.id()),
                new ProjectName(projectDocument.name()),
                FractalFormulaFactory.create(FractalFormulaType.valueOf(projectDocument.fractalFormulaType())),
                timeline,
                new RenderProfile(
                        projectDocument.renderProfile().name(),
                        new Resolution(projectDocument.renderProfile().width(), projectDocument.renderProfile().height()),
                        new EscapeParameters(
                                projectDocument.renderProfile().maxIterations(),
                                projectDocument.renderProfile().escapeRadius()
                        ),
                        RenderQuality.valueOf(projectDocument.renderProfile().quality())
                ),
                new ColorProfile(
                        projectDocument.colorProfile().name(),
                        new Palette(projectDocument.colorProfile().colorStops().stream()
                                .map(stop -> new ColorStop(stop.position(), new RgbColor(stop.red(), stop.green(), stop.blue())))
                                .toList())
                ),
                new ProjectMetadata(
                        Instant.parse(metadataDocument.createdAt()),
                        Instant.parse(metadataDocument.updatedAt()),
                        metadataDocument.description()
                ),
                new ProjectSettings(
                        settingsDocument.defaultFramesPerSecond(),
                        settingsDocument.defaultRenderFrameCount(),
                        settingsDocument.keyframeStepSeconds(),
                        settingsDocument.defaultRenderPreset() == null
                                ? RenderPreset.STANDARD
                                : RenderPreset.valueOf(settingsDocument.defaultRenderPreset())
                ),
                bookmarkDocuments.stream()
                        .map(bookmark -> new ProjectBookmark(
                                new ProjectBookmarkId(bookmark.id()),
                                bookmark.label(),
                                new CameraState(
                                        new FractalCoordinate(bookmark.centerX(), bookmark.centerY()),
                                        new ZoomLevel(bookmark.zoom())
                                )
                        ))
                        .toList()
        );
    }
}
