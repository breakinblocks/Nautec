package com.breakinblocks.nautec.client.render;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class NTRenderTypes {
    private static final RenderType SONAR_HIGHLIGHT = RenderType.create("nautec_sonar_highlight",
            RenderSetup.builder(NTRenderPipelines.SONAR_HIGHLIGHT).createRenderSetup());

    private static final RenderType SPOTLIGHT_CONE = RenderType.create("nautec_spotlight_cone",
            RenderSetup.builder(NTRenderPipelines.SPOTLIGHT_CONE).sortOnUpload().createRenderSetup());

    private static final RenderType LASER_BEAM = RenderType.create("nautec_laser_beam",
            RenderSetup.builder(NTRenderPipelines.LASER_BEAM).createRenderSetup());

    private static final RenderType LASER_FLARE = RenderType.create("nautec_laser_flare",
            RenderSetup.builder(NTRenderPipelines.LASER_FLARE).createRenderSetup());

    public static RenderType laserBeam() {
        return LASER_BEAM;
    }

    public static RenderType laserFlare() {
        return LASER_FLARE;
    }

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
