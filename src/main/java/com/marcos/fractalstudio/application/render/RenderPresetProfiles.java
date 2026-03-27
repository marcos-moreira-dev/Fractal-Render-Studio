package com.marcos.fractalstudio.application.render;

import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.RenderPreset;
import com.marcos.fractalstudio.domain.render.RenderProfile;
import com.marcos.fractalstudio.domain.render.RenderQuality;
import com.marcos.fractalstudio.domain.render.Resolution;

/**
 * Maps a user-facing render preset to an effective render profile derived from a project base profile.
 */
public final class RenderPresetProfiles {

    private RenderPresetProfiles() {
    }

    /**
     * Applies a preset-specific iteration and quality policy to a base profile.
     *
     * @param baseProfile project base profile
     * @param renderPreset requested preset
     * @return effective profile used by the renderer
     */
    public static RenderProfile apply(RenderProfile baseProfile, RenderPreset renderPreset) {
        return switch (renderPreset) {
            case DRAFT -> new RenderProfile(
                    "Draft",
                    baseProfile.resolution(),
                    new EscapeParameters(Math.max(64, baseProfile.escapeParameters().maxIterations() / 2), baseProfile.escapeParameters().escapeRadius()),
                    RenderQuality.PREVIEW
            );
            case STANDARD -> new RenderProfile(
                    "Standard",
                    atLeastFullHd(baseProfile.resolution()),
                    baseProfile.escapeParameters(),
                    RenderQuality.FINAL
            );
            case DEEP_ZOOM -> new RenderProfile(
                    "Deep Zoom",
                    atLeastFullHd(baseProfile.resolution()),
                    new EscapeParameters(Math.max(512, baseProfile.escapeParameters().maxIterations() * 2), baseProfile.escapeParameters().escapeRadius()),
                    RenderQuality.FINAL
            );
        };
    }

    private static Resolution atLeastFullHd(Resolution baseResolution) {
        double scale = Math.max(1920.0 / baseResolution.width(), 1080.0 / baseResolution.height());
        if (scale <= 1.0) {
            return baseResolution;
        }
        return new Resolution(
                (int) Math.round(baseResolution.width() * scale),
                (int) Math.round(baseResolution.height() * scale)
        );
    }
}
