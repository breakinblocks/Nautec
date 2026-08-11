package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public final class PortalRenderer {
    public static final Identifier SPIRAL = Nautec.rl("textures/effect/teleport_spiral.png");

    private static final float[] RADII = {3.0F, 2.4F, 1.8F, 1.1F};
    private static final int[] ALPHA = {70, 110, 160, 220};
    private static final int COLOR = 0xFFB07AFF;
    private static final int FULL_BRIGHT = 15728880;

    private PortalRenderer() {
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPos, float partialTick) {
        if (!TeleportFxManager.isVisible()) {
            return;
        }

        float progress = TeleportFxManager.openProgress(partialTick);
        if (progress <= 0F) {
            return;
        }

        Vec3 at = TeleportFxManager.portalPos();
        float yaw = TeleportFxManager.portalYaw();
        float spin = (net.minecraft.client.Minecraft.getInstance().level == null ? 0F
                : net.minecraft.client.Minecraft.getInstance().level.getGameTime() + partialTick) * 2.5F;

        poseStack.pushPose();
        poseStack.translate(at.x - cameraPos.x, at.y - cameraPos.y, at.z - cameraPos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180F - yaw));

        for (int layer = 0; layer < RADII.length; layer++) {
            float radius = RADII[layer] * progress;
            int color = ARGB.color((int) (ALPHA[layer] * progress), COLOR);
            float rotation = layer % 2 == 0 ? spin : -spin * 1.4F;

            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            collector.submitCustomGeometry(poseStack, NTRenderTypes.portalSwirl(SPIRAL),
                    (pose, buffer) -> quad(pose, buffer, radius, color));
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float radius, int color) {
        vertex(pose, buffer, -radius, -radius, color, 0F, 1F);
        vertex(pose, buffer, radius, -radius, color, 1F, 1F);
        vertex(pose, buffer, radius, radius, color, 1F, 0F);
        vertex(pose, buffer, -radius, radius, color, 0F, 0F);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, int color, float u, float v) {
        buffer.addVertex(pose, x, y, 0F)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(pose, 0F, 0F, 1F);
    }
}
