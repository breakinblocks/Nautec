package com.breakinblocks.nautec.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class NTRenderTypes {
    private static final RenderStateShard.ShaderStateShard CRYSTAL_SHELL_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::crystalShell);
    private static final RenderStateShard.ShaderStateShard CRYSTAL_CORE_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::crystalCore);
    private static final RenderStateShard.ShaderStateShard CRYSTAL_HALO_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::crystalHalo);
    private static final RenderStateShard.ShaderStateShard GATEWAY_GLOW_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::gatewayGlow);
    private static final RenderStateShard.ShaderStateShard GATEWAY_HORIZON_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::gatewayHorizon);
    private static final RenderStateShard.ShaderStateShard FUSION_PLASMA_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::fusionPlasma);
    private static final RenderStateShard.ShaderStateShard FUSION_FIELD_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::fusionField);
    private static final RenderStateShard.ShaderStateShard LASER_BEAM_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::laserBeam);
    private static final RenderStateShard.ShaderStateShard LASER_FLARE_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::laserFlare);
    private static final RenderStateShard.ShaderStateShard SONAR_RESULT_SHADER = new RenderStateShard.ShaderStateShard(NTShaders::sonarResult);

    private static final RenderType SONAR_HIGHLIGHT = RenderType.create("nautec_sonar_highlight",
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 1536, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(SONAR_RESULT_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private static final RenderType SPOTLIGHT_CONE = RenderType.create("nautec_spotlight_cone",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private static final RenderType REACTOR_GLOW = RenderType.create("nautec_reactor_glow",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private static final RenderType LASER_BEAM = glow("nautec_laser_beam", LASER_BEAM_SHADER, RenderStateShard.ADDITIVE_TRANSPARENCY, false);

    private static final RenderType LASER_FLARE = glow("nautec_laser_flare", LASER_FLARE_SHADER, RenderStateShard.ADDITIVE_TRANSPARENCY, false);

    private static final RenderType FUSION_PLASMA = glow("nautec_fusion_plasma", FUSION_PLASMA_SHADER, RenderStateShard.LIGHTNING_TRANSPARENCY, false);

    private static final RenderType FUSION_FIELD = glow("nautec_fusion_field", FUSION_FIELD_SHADER, RenderStateShard.LIGHTNING_TRANSPARENCY, false);

    private static final RenderType GATEWAY_HORIZON = glow("nautec_gateway_horizon", GATEWAY_HORIZON_SHADER, RenderStateShard.TRANSLUCENT_TRANSPARENCY, true);

    private static final RenderType CRYSTAL_HALO = RenderType.create("nautec_crystal_halo",
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 1536, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(CRYSTAL_HALO_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false));

    private static final Function<ResourceLocation, RenderType> CRYSTAL_SHELL = Util.memoize(texture -> crystal("nautec_crystal_shell",
            CRYSTAL_SHELL_SHADER, texture, RenderStateShard.TRANSLUCENT_TRANSPARENCY, RenderStateShard.NO_LAYERING, true));

    private static final Function<ResourceLocation, RenderType> CRYSTAL_CORE = Util.memoize(texture -> crystal("nautec_crystal_core",
            CRYSTAL_CORE_SHADER, texture, RenderStateShard.ADDITIVE_TRANSPARENCY, RenderStateShard.NO_LAYERING, false));

    private static final Function<ResourceLocation, RenderType> GATEWAY_GLOW = Util.memoize(texture -> crystal("nautec_gateway_glow",
            GATEWAY_GLOW_SHADER, texture, RenderStateShard.ADDITIVE_TRANSPARENCY, RenderStateShard.POLYGON_OFFSET_LAYERING, false));

    private static final Function<ResourceLocation, RenderType> EMISSIVE_OVERLAY = Util.memoize(texture -> RenderType.create("nautec_emissive_overlay",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(true)));

    private static final Function<ResourceLocation, RenderType> EYES_OVERLAY = Util.memoize(texture -> RenderType.create("nautec_eyes_overlay",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_EYES_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false)));

    private static RenderType glow(String name, RenderStateShard.ShaderStateShard shader, RenderStateShard.TransparencyStateShard transparency, boolean sort) {
        return RenderType.create(name, DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 1536, false, sort,
                RenderType.CompositeState.builder()
                        .setShaderState(shader)
                        .setTransparencyState(transparency)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setCullState(RenderStateShard.NO_CULL)
                        .createCompositeState(false));
    }

    private static RenderType crystal(String name, RenderStateShard.ShaderStateShard shader, ResourceLocation texture,
                                      RenderStateShard.TransparencyStateShard transparency, RenderStateShard.LayeringStateShard layering, boolean sort) {
        return RenderType.create(name, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, VertexFormat.Mode.QUADS, 1536, false, sort,
                RenderType.CompositeState.builder()
                        .setShaderState(shader)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(transparency)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLayeringState(layering)
                        .createCompositeState(false));
    }

    public static RenderType emissiveOverlay(ResourceLocation texture) {
        return EMISSIVE_OVERLAY.apply(texture);
    }

    public static RenderType eyesOverlay(ResourceLocation texture) {
        return EYES_OVERLAY.apply(texture);
    }

    public static RenderType fusionPlasma() {
        return FUSION_PLASMA;
    }

    public static RenderType fusionField() {
        return FUSION_FIELD;
    }

    public static RenderType gatewayGlow(ResourceLocation texture) {
        return GATEWAY_GLOW.apply(texture);
    }

    public static RenderType gatewayHorizon() {
        return GATEWAY_HORIZON;
    }

    public static RenderType crystalShell(ResourceLocation texture) {
        return CRYSTAL_SHELL.apply(texture);
    }

    public static RenderType crystalCore(ResourceLocation texture) {
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

    public static RenderType portalSwirl(ResourceLocation texture) {
        return RenderType.eyes(texture);
    }

    private NTRenderTypes() {
    }
}
