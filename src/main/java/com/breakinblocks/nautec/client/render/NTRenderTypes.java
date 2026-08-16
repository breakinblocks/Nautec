package com.breakinblocks.nautec.client.render;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class NTRenderTypes {
    private static final RenderType SONAR_HIGHLIGHT = RenderType.create("nautec_sonar_highlight",
            RenderSetup.builder(NTRenderPipelines.SONAR_HIGHLIGHT).sortOnUpload().createRenderSetup());

    private static final RenderType SPOTLIGHT_CONE = RenderType.create("nautec_spotlight_cone",
            RenderSetup.builder(NTRenderPipelines.SPOTLIGHT_CONE).sortOnUpload().createRenderSetup());

    public static RenderType sonarHighlight() {
        return SONAR_HIGHLIGHT;
    }

    public static RenderType spotlightCone() {
        return SPOTLIGHT_CONE;
    }

    public static RenderType portalSwirl(Identifier texture) {
        return RenderTypes.eyes(texture);
    }

    private NTRenderTypes() {
    }
}
