package com.breakinblocks.nautec.client.sonar;

import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SonarPulseRenderer {
    private static final int SEGMENTS = 72;
    private static final int TRAIL_RINGS = 3;
    private static final float TRAIL_SPACING = 1.6F;
    private static final float BAND_HEIGHT = 1.1F;
    private static final float PEAK_ALPHA = 0.5F;
    private static final int COLOR = 0x3EFDFF;

    private SonarPulseRenderer() {
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPos) {
        if (!NautecSonarManager.isActive()) {
            return;
        }

        float leading = NautecSonarManager.pulseRadius();
        float range = NautecSonarManager.range();
        if (leading <= 0.05F || range <= 0F) {
            return;
        }

        Vec3 centre = NautecSonarManager.center();
        float fade = NautecSonarManager.fade();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        collector.submitCustomGeometry(poseStack, NTRenderTypes.sonarHighlight(), (pose, buffer) -> {
            for (int ring = 0; ring < TRAIL_RINGS; ring++) {
                float radius = leading - ring * TRAIL_SPACING;
                if (radius <= 0.05F) {
                    continue;
                }

                float travelled = Mth.clamp(radius / range, 0F, 1F);
                float alpha = PEAK_ALPHA * fade * (1F - travelled) / (1F + ring * 1.5F);
                if (alpha <= 0.004F) {
                    continue;
                }

                ring(pose, buffer, centre, radius, ARGB.color((int) (alpha * 255F), COLOR));
            }
        });

        poseStack.popPose();
    }

    private static void ring(PoseStack.Pose pose, VertexConsumer buffer, Vec3 centre, float radius, int color) {
        float top = (float) centre.y + BAND_HEIGHT;
        float bottom = (float) centre.y - BAND_HEIGHT;

        for (int segment = 0; segment < SEGMENTS; segment++) {
            double from = segment * Mth.TWO_PI / SEGMENTS;
            double to = (segment + 1) * Mth.TWO_PI / SEGMENTS;

            float x0 = (float) (centre.x + Math.cos(from) * radius);
            float z0 = (float) (centre.z + Math.sin(from) * radius);
            float x1 = (float) (centre.x + Math.cos(to) * radius);
            float z1 = (float) (centre.z + Math.sin(to) * radius);

            buffer.addVertex(pose, x0, bottom, z0).setColor(color);
            buffer.addVertex(pose, x0, top, z0).setColor(color);
            buffer.addVertex(pose, x1, top, z1).setColor(color);
            buffer.addVertex(pose, x1, bottom, z1).setColor(color);
        }
    }
}
