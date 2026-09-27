package com.breakinblocks.nautec.client.sonar;

import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SonarHighlightRenderer {
    private static @Nullable ByteBufferBuilder renderBuffer;
    private record VisibleBox(AABB box, int color) { }

    private SonarHighlightRenderer() {
    }

    public static void render(PoseStack poseStack, Vec3 cameraPos, float partialTick) {
        if (!NautecSonarManager.isActive()) {
            return;
        }

        Map<VisibleBox, Float> boxes = new HashMap<>();
        for (SonarScan scan : NautecSonarManager.scans()) {
            collect(boxes, scan.marks(), scan.pulseRadius(partialTick), partialTick, scan.fade() * 0.45F);
            collect(boxes, scan.hostiles(), scan.pulseRadius(partialTick), partialTick, scan.fade() * 0.55F);
        }
        Vec3 offset = cameraPos.reverse();

        if (renderBuffer == null) renderBuffer = new ByteBufferBuilder(4096);
        MultiBufferSource.BufferSource source = MultiBufferSource.immediate(renderBuffer);
        VertexConsumer buffer = source.getBuffer(NTRenderTypes.sonarHighlight());
        PoseStack.Pose pose = poseStack.last();
        for (Map.Entry<VisibleBox, Float> entry : boxes.entrySet()) {
            VisibleBox visible = entry.getKey();
            box(pose, buffer, visible.box().move(offset), visible.color(), entry.getValue());
        }
        source.endBatch();
    }

    private static void collect(Map<VisibleBox, Float> boxes, List<NautecSonarManager.Mark> marks,
                                float pulse, float partialTick, float alpha) {
        for (NautecSonarManager.Mark mark : marks) {
            if (mark.revealAt() <= pulse) {
                boxes.merge(new VisibleBox(mark.interpolatedBox(partialTick), mark.color()), alpha, Math::max);
            }
        }
    }

    public static void close() {
        if (renderBuffer != null) {
            renderBuffer.close();
            renderBuffer = null;
        }
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
        buffer.addVertex(pose, x0, y0, z0).setUv(0F, 0F).setColor(color);
        buffer.addVertex(pose, x1, y1, z1).setUv(0F, 1F).setColor(color);
        buffer.addVertex(pose, x2, y2, z2).setUv(1F, 1F).setColor(color);
        buffer.addVertex(pose, x3, y3, z3).setUv(1F, 0F).setColor(color);
    }
}
