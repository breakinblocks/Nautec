package com.breakinblocks.nautec.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.Optional;

public final class NTRenderPipelines {
    public static final RenderPipeline TELEPORT_BLUR = RenderPipeline.builder()
            .withLocation(Nautec.rl("pipeline/teleport_blur"))
            .withVertexShader("core/screenquad")
            .withFragmentShader(Nautec.rl("core/teleport_blur"))
            .withSampler("SceneSampler")
            .withUniform("BlurInfo", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
            .build();

    public static final RenderPipeline SONAR_HIGHLIGHT = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(Nautec.rl("pipeline/sonar_highlight"))
            .withVertexShader("core/position_tex_color")
            .withFragmentShader(Nautec.rl("core/sonar_result"))
            .withUniform("Globals", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withCull(false)
            .build();

    public static final RenderPipeline SONAR_WAVE = RenderPipeline.builder()
            .withLocation(Nautec.rl("pipeline/sonar_wave"))
            .withVertexShader("core/screenquad")
            .withFragmentShader(Nautec.rl("core/sonar_wave"))
            .withSampler("DepthSampler")
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("ScanInfo", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
            .build();

    public static final RenderPipeline TIDAL_SHOCKWAVE = RenderPipeline.builder()
            .withLocation(Nautec.rl("pipeline/tidal_shockwave"))
            .withVertexShader("core/screenquad")
            .withFragmentShader(Nautec.rl("core/tidal_shockwave"))
            .withSampler("DepthSampler")
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("ShockwaveInfo", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
            .build();

    public static final RenderPipeline PORTAL_ARRIVAL = RenderPipeline.builder()
            .withLocation(Nautec.rl("pipeline/portal_arrival"))
            .withVertexShader("core/screenquad")
            .withFragmentShader(Nautec.rl("core/portal_arrival"))
            .withSampler("DepthSampler")
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("ArrivalInfo", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
            .build();

    public static final RenderPipeline SPOTLIGHT_CONE =RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(Nautec.rl("pipeline/spotlight_cone"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withCull(false)
            .build();

    public static final RenderPipeline LASER_BEAM = laser("laser_beam");

    public static final RenderPipeline LASER_FLARE = laser("laser_flare");

    private static RenderPipeline laser(String name) {
        return RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(Nautec.rl("pipeline/" + name))
                .withVertexShader("core/position_tex_color")
                .withFragmentShader(Nautec.rl("core/" + name))
                .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withCull(false)
                .build();
    }

    private NTRenderPipelines() {
    }
}
