package com.marcos.fractalstudio.application.render;

import com.marcos.fractalstudio.domain.render.EscapeParameters;
import com.marcos.fractalstudio.domain.render.RenderPreset;
import com.marcos.fractalstudio.domain.render.RenderProfile;
import com.marcos.fractalstudio.domain.render.RenderQuality;
import com.marcos.fractalstudio.domain.render.Resolution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RenderPresetProfilesTest {

    @Test
    void appliesDraftPresetWithLighterBudget() {
        RenderProfile baseProfile = new RenderProfile(
                "Base",
                new Resolution(640, 360),
                new EscapeParameters(256, 4.0),
                RenderQuality.FINAL
        );

        RenderProfile draftProfile = RenderPresetProfiles.apply(baseProfile, RenderPreset.DRAFT);

        assertEquals(RenderQuality.PREVIEW, draftProfile.quality());
        assertTrue(draftProfile.escapeParameters().maxIterations() < baseProfile.escapeParameters().maxIterations());
    }

    @Test
    void appliesDeepZoomPresetWithHeavierBudget() {
        RenderProfile baseProfile = new RenderProfile(
                "Base",
                new Resolution(640, 360),
                new EscapeParameters(256, 4.0),
                RenderQuality.FINAL
        );

        RenderProfile deepZoomProfile = RenderPresetProfiles.apply(baseProfile, RenderPreset.DEEP_ZOOM);

        assertEquals(RenderQuality.FINAL, deepZoomProfile.quality());
        assertTrue(deepZoomProfile.escapeParameters().maxIterations() > baseProfile.escapeParameters().maxIterations());
    }
}
