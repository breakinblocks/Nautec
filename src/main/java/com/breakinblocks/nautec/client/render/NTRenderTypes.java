package com.breakinblocks.nautec.client.render;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public final class NTRenderTypes {
    private static final RenderType SONAR_HIGHLIGHT = RenderType.create("nautec_sonar_highlight",
            RenderSetup.builder(NTRenderPipelines.SONAR_HIGHLIGHT).createRenderSetup());

    private static final RenderType SPOTLIGHT_CONE = RenderType.create("nautec_spotlight_cone",
            RenderSetup.builder(NTRenderPipelines.SPOTLIGHT_CONE).sortOnUpload().createRenderSetup());

    private static final RenderType LASER_BEAM = RenderType.create("nautec_laser_beam",
            RenderSetup.builder(NTRenderPipelines.LASER_BEAM).createRenderSetup());

    private static final RenderType LASER_FLARE = RenderType.create("nautec_laser_flare",
            RenderSetup.builder(NTRenderPipelines.LASER_FLARE).createRenderSetup());

    private static final RenderType REACTOR_GLOW = RenderType.create("nautec_reactor_glow",
            RenderSetup.builder(NTRenderPipelines.REACTOR_GLOW).createRenderSetup());

    private static final Function<Identifier, RenderType> CRYSTAL_SHELL = Util.memoize(texture -> RenderType.create("nautec_crystal_shell",
            RenderSetup.builder(NTRenderPipelines.CRYSTAL_SHELL).withTexture("Sampler0", texture).sortOnUpload().createRenderSetup()));

    private static final Function<Identifier, RenderType> CRYSTAL_CORE = Util.memoize(texture -> RenderType.create("nautec_crystal_core",
            RenderSetup.builder(NTRenderPipelines.CRYSTAL_CORE).withTexture("Sampler0", texture).createRenderSetup()));

    private static final RenderType CRYSTAL_HALO = RenderType.create("nautec_crystal_halo",
            RenderSetup.builder(NTRenderPipelines.CRYSTAL_HALO).createRenderSetup());

    private static final Function<Identifier, RenderType> GATEWAY_GLOW = Util.memoize(texture -> RenderType.create("nautec_gateway_glow",
            RenderSetup.builder(NTRenderPipelines.GATEWAY_GLOW).withTexture("Sampler0", texture).createRenderSetup()));

    private static final RenderType GATEWAY_HORIZON = RenderType.create("nautec_gateway_horizon",
            RenderSetup.builder(NTRenderPipelines.GATEWAY_HORIZON).sortOnUpload().createRenderSetup());

    private static final RenderType FUSION_PLASMA = RenderType.create("nautec_fusion_plasma",
            RenderSetup.builder(NTRenderPipelines.FUSION_PLASMA).createRenderSetup());

    private static final RenderType FUSION_FIELD = RenderType.create("nautec_fusion_field",
            RenderSetup.builder(NTRenderPipelines.FUSION_FIELD).createRenderSetup());

    private static final Function<Identifier, RenderType> EMISSIVE_OVERLAY = Util.memoize(texture -> RenderType.create("nautec_emissive_overlay",
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE)
                    .withTexture("Sampler0", texture)
                    .useOverlay()
                    .affectsCrumbling()
                    .sortOnUpload()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()));

    private static final Function<Identifier, RenderType> EYES_OVERLAY = Util.memoize(texture -> RenderType.create("nautec_eyes_overlay",
            RenderSetup.builder(RenderPipelines.EYES)
                    .withTexture("Sampler0", texture)
                    .sortOnUpload()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()));

    public static RenderType emissiveOverlay(Identifier texture) {
        return EMISSIVE_OVERLAY.apply(texture);
    }

    public static RenderType eyesOverlay(Identifier texture) {
        return EYES_OVERLAY.apply(texture);
    }

    public static RenderType fusionPlasma() {
        return FUSION_PLASMA;
    }

    public static RenderType fusionField() {
        return FUSION_FIELD;
    }

    public static RenderType gatewayGlow(Identifier texture) {
        return GATEWAY_GLOW.apply(texture);
    }

    public static RenderType gatewayHorizon() {
        return GATEWAY_HORIZON;
    }

    public static RenderType crystalShell(Identifier texture) {
        return CRYSTAL_SHELL.apply(texture);
    }

    public static RenderType crystalCore(Identifier texture) {
        return CRYSTAL_CORE.apply(texture);
    }

    public static RenderType crystalHalo() {
        return CRYSTAL_HALO;
    }

    public static RenderType reactorGlow() {
        return REACTOR_GLOW;
    }

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
