package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

public final class ResonantLights {
    private static final Identifier TEXTURE = Nautec.rl("textures/block/resonant_light.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TEXTURE);
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float[] LEFT = {3.5f, 6.0f, 8.5f, 11.0f};
    private static final float WIDTH = 1.5f;

    private ResonantLights() {
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Direction facing, GatewayAddress address,
                              float frontZ, float bottom, float top) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
        poseStack.translate(-0.5, 0, -0.5);
        int[] colours = new int[GatewayAddress.SLOTS];
        for (int slot = 0; slot < GatewayAddress.SLOTS; slot++) {
            DyeColor dye = address.slots().get(slot);
            colours[slot] = dye.getTextureDiffuseColor();
        }
        float z = frontZ / 16f - 0.002f;
        float y0 = bottom / 16f;
        float y1 = top / 16f;
        collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, consumer) -> {
            for (int slot = 0; slot < colours.length; slot++) {
                float left = LEFT[LEFT.length - 1 - slot];
                float x0 = left / 16f;
                float x1 = (left + WIDTH) / 16f;
                quad(pose, consumer, colours[slot], x0, x1, y0, y1, z);
            }
        });
        poseStack.popPose();
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, int colour, float x0, float x1, float y0, float y1, float z) {
        int r = colour >> 16 & 255;
        int g = colour >> 8 & 255;
        int b = colour & 255;
        consumer.addVertex(pose, x1, y0, z).setColor(r, g, b, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 0, -1);
        consumer.addVertex(pose, x0, y0, z).setColor(r, g, b, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 0, -1);
        consumer.addVertex(pose, x0, y1, z).setColor(r, g, b, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 0, -1);
        consumer.addVertex(pose, x1, y1, z).setColor(r, g, b, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 0, -1);
    }
}
