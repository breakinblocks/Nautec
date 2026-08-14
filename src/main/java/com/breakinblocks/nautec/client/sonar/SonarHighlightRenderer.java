package com.breakinblocks.nautec.client.sonar;

import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.SubmitNodeCollector;

public final class SonarHighlightRenderer {
    private SonarHighlightRenderer() {
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPos) {
        if (!NautecSonarManager.isActive()) {
            return;
        }

        float fade = NautecSonarManager.fade();
        float pulse = NautecSonarManager.pulseRadius();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        collector.submitCustomGeometry(poseStack, NTRenderTypes.sonarHighlight(), (pose, buffer) -> {
            for (NautecSonarManager.Mark mark : NautecSonarManager.marks()) {
                if (mark.revealAt() > pulse) {
                    continue;
                }
                box(pose, buffer, mark.box(), mark.color(), fade * 0.45F);
            }

            for (NautecSonarManager.Mark hostile : NautecSonarManager.hostiles()) {
                box(pose, buffer, hostile.box(), hostile.color(), fade * 0.55F);
            }
        });

        poseStack.popPose();
    }

    private static void box(PoseStack.Pose pose, VertexConsumer buffer, AABB box, int color, float alpha) {
        int tinted = ARGB.color((int) (alpha * 255F), color);

        float x0 = (float) box.minX;
        float y0 = (float) box.minY;
        float z0 = (float) box.minZ;
        float x1 = (float) box.maxX;
        float y1 = (float) box.maxY;
        float z1 = (float) box.maxZ;

        quad(pose, buffer, tinted, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        quad(pose, buffer, tinted, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);
        quad(pose, buffer, tinted, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0);
        quad(pose, buffer, tinted, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
        quad(pose, buffer, tinted, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        quad(pose, buffer, tinted, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int color,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3) {
        buffer.addVertex(pose, x0, y0, z0).setColor(color);
        buffer.addVertex(pose, x1, y1, z1).setColor(color);
        buffer.addVertex(pose, x2, y2, z2).setColor(color);
        buffer.addVertex(pose, x3, y3, z3).setColor(color);
    }
}
