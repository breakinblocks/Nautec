package com.breakinblocks.nautec.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class CustomGeometry {
    private CustomGeometry() {
    }

    public static void submit(PoseStack poseStack, MultiBufferSource buffers, RenderType renderType, ShaderPackOverlay.Geometry renderer) {
        renderer.render(poseStack.last(), buffers.getBuffer(renderType));
    }
}
