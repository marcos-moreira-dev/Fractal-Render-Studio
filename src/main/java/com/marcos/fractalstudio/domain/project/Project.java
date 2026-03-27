package com.marcos.fractalstudio.domain.project;

import com.marcos.fractalstudio.domain.color.ColorProfile;
import com.marcos.fractalstudio.domain.fractal.FractalFormula;
import com.marcos.fractalstudio.domain.render.RenderProfile;
import com.marcos.fractalstudio.domain.timeline.Timeline;
import com.marcos.fractalstudio.domain.validation.ProjectRenderabilityValidator;

import java.util.List;
import java.util.Objects;

/**
 * Aggregate root that represents the full state of a fractal exploration project.
 *
 * <p>The project concentrates the mathematical model that the user is editing:
 *
 * <ul>
 *   <li>the fractal formula to evaluate</li>
 *   <li>the visual color profile</li>
 *   <li>the render profile and quality defaults</li>
 *   <li>the timeline used to derive animation frames</li>
 *   <li>the reusable saved points exposed as bookmarks</li>
 *   <li>metadata and user-facing project settings</li>
 * </ul>
 *
 * <p>The aggregate is intentionally immutable. Any meaningful change produces a
 * new {@code Project} instance so that callers can reason about state updates
 * explicitly and the application layer can coordinate persistence, preview
 * invalidation and timeline regeneration without hidden mutation.
 */
public final class Project {

    private final ProjectId id;
    private final ProjectName name;
    private final FractalFormula fractalFormula;
    private final Timeline timeline;
    private final RenderProfile renderProfile;
    private final ColorProfile colorProfile;
    private final ProjectMetadata metadata;
    private final ProjectSettings settings;
    private final List<ProjectBookmark> bookmarks;

    /**
     * Creates a fully-specified project aggregate.
     *
     * <p>The constructor is strict on nullability because a partially-built
     * project would immediately complicate renderability checks and persistence
     * mapping. Collections are defensively copied to preserve immutability at
     * the aggregate boundary.
     */
    public Project(
            ProjectId id,
            ProjectName name,
            FractalFormula fractalFormula,
            Timeline timeline,
            RenderProfile renderProfile,
            ColorProfile colorProfile,
            ProjectMetadata metadata,
            ProjectSettings settings,
            List<ProjectBookmark> bookmarks
    ) {
        this.id = Objects.requireNonNull(id, "Project id is required.");
        this.name = Objects.requireNonNull(name, "Project name is required.");
        this.fractalFormula = Objects.requireNonNull(fractalFormula, "Fractal formula is required.");
        this.timeline = Objects.requireNonNull(timeline, "Timeline is required.");
        this.renderProfile = Objects.requireNonNull(renderProfile, "Render profile is required.");
        this.colorProfile = Objects.requireNonNull(colorProfile, "Color profile is required.");
        this.metadata = Objects.requireNonNull(metadata, "Project metadata is required.");
        this.settings = Objects.requireNonNull(settings, "Project settings are required.");
        this.bookmarks = List.copyOf(Objects.requireNonNull(bookmarks, "Project bookmarks are required."));
    }

    public Project rename(ProjectName newName) {
        return new Project(id, newName, fractalFormula, timeline, renderProfile, colorProfile, metadata.touch(), settings, bookmarks);
    }

    /**
     * Returns a new aggregate with an updated fractal formula.
     *
     * <p>This operation keeps the current timeline and saved points intact on
     * purpose: changing the formula should preserve the camera journey while
     * forcing the renderer to reinterpret the same mathematical region with a
     * different escape-time function.
     */
    public Project withFractalFormula(FractalFormula newFractalFormula) {
        return new Project(id, name, newFractalFormula, timeline, renderProfile, colorProfile, metadata.touch(), settings, bookmarks);
    }

    public Project withTimeline(Timeline newTimeline) {
        return new Project(id, name, fractalFormula, newTimeline, renderProfile, colorProfile, metadata.touch(), settings, bookmarks);
    }

    public Project withRenderProfile(RenderProfile newRenderProfile) {
        return new Project(id, name, fractalFormula, timeline, newRenderProfile, colorProfile, metadata.touch(), settings, bookmarks);
    }

    public Project withColorProfile(ColorProfile newColorProfile) {
        return new Project(id, name, fractalFormula, timeline, renderProfile, newColorProfile, metadata.touch(), settings, bookmarks);
    }

    public Project withMetadata(ProjectMetadata newMetadata) {
        return new Project(id, name, fractalFormula, timeline, renderProfile, colorProfile, newMetadata, settings, bookmarks);
    }

    public Project withSettings(ProjectSettings newSettings) {
        return new Project(id, name, fractalFormula, timeline, renderProfile, colorProfile, metadata.touch(), newSettings, bookmarks);
    }

    public Project withBookmarks(List<ProjectBookmark> newBookmarks) {
        return new Project(id, name, fractalFormula, timeline, renderProfile, colorProfile, metadata.touch(), settings, newBookmarks);
    }

    /**
     * Validates whether the project contains the minimum coherent information
     * required to produce a render plan.
     *
     * <p>The actual rules live in {@link ProjectRenderabilityValidator} so that
     * validation remains testable and reusable outside the aggregate itself.
     *
     * @throws IllegalStateException when the project cannot be rendered safely
     */
    public void validateRenderability() {
        new ProjectRenderabilityValidator().validate(this);
    }

    /**
     * Convenience predicate used by the application and presentation layers to
     * query renderability without handling exceptions directly.
     *
     * @return {@code true} when the project passes all renderability checks
     */
    public boolean isRenderable() {
        try {
            validateRenderability();
            return true;
        } catch (IllegalStateException exception) {
            return false;
        }
    }

    public ProjectId id() {
        return id;
    }

    public ProjectName name() {
        return name;
    }

    public FractalFormula fractalFormula() {
        return fractalFormula;
    }

    public Timeline timeline() {
        return timeline;
    }

    public RenderProfile renderProfile() {
        return renderProfile;
    }

    public ColorProfile colorProfile() {
        return colorProfile;
    }

    public ProjectMetadata metadata() {
        return metadata;
    }

    public ProjectSettings settings() {
        return settings;
    }

    public List<ProjectBookmark> bookmarks() {
        return bookmarks;
    }
}
