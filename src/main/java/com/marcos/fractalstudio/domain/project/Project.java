package com.marcos.fractalstudio.domain.project;

import com.marcos.fractalstudio.domain.color.ColorProfile;
import com.marcos.fractalstudio.domain.fractal.FractalFormula;
import com.marcos.fractalstudio.domain.render.RenderProfile;
import com.marcos.fractalstudio.domain.timeline.Timeline;
import com.marcos.fractalstudio.domain.validation.ProjectRenderabilityValidator;

import java.util.List;
import java.util.Objects;

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

    public void validateRenderability() {
        new ProjectRenderabilityValidator().validate(this);
    }

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
