package com.breakinblocks.nautec.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class LaserBeamRenderer {
    public static final int CYAN = 0xFF38E4FF;

    private static final float MIN_LENGTH = 1.0E-4F;

    private LaserBeamRenderer() {
    }

    public static void submitBeam(PoseStack poseStack, SubmitNodeCollector collector, Vector3fc from, Vector3fc to,
                                  float halfWidth, int color) {
        submitBeam(poseStack, collector, from, to, halfWidth, color, false);
    }

    public static void submitBeam(PoseStack poseStack, SubmitNodeCollector collector, Vector3fc from, Vector3fc to,
                                  float halfWidth, int color, boolean world) {
        Matrix4f pose = poseStack.last().pose();
        float scale = Math.max(1.0E-4F, pose.getScale(new Vector3f()).x);
        Vector3f camera = cameraInPose(pose);

        Vector3f axis = new Vector3f(to).sub(from);
        float length = axis.length();
        if (length < MIN_LENGTH) {
            return;
        }
        axis.div(length);

        float width = halfWidth / scale;
        Vector3f sideFrom = side(axis, from, camera, width);
        Vector3f sideTo = side(axis, to, camera, width);
        float worldLength = length * scale;

        submit(poseStack, collector, NTRenderTypes.laserBeam(), world, (last, buffer) -> {
            vertex(buffer, last, new Vector3f(from).sub(sideFrom), 0F, 0F, color);
            vertex(buffer, last, new Vector3f(from).add(sideFrom), 1F, 0F, color);
            vertex(buffer, last, new Vector3f(to).add(sideTo), 1F, worldLength, color);
            vertex(buffer, last, new Vector3f(to).sub(sideTo), 0F, worldLength, color);
        });
    }

    public static void submitFlare(PoseStack poseStack, SubmitNodeCollector collector, Vector3fc centre,
                                   float radius, int color) {
        submitFlare(poseStack, collector, centre, radius, color, false);
    }

    public static void submitFlare(PoseStack poseStack, SubmitNodeCollector collector, Vector3fc centre,
                                   float radius, int color, boolean world) {
        Matrix4f pose = poseStack.last().pose();
        float scale = Math.max(1.0E-4F, pose.getScale(new Vector3f()).x);
        Vector3f camera = cameraInPose(pose);

        Vector3f normal = new Vector3f(camera).sub(centre);
        float distance = normal.length();
        if (distance < MIN_LENGTH) {
            return;
        }
        normal.div(distance);

        float size = radius / scale;
        Vector3f middle = new Vector3f(normal).mul(Math.min(size, distance * 0.5F)).add(centre);
        Vector3f reference = Math.abs(normal.y) < 0.99F ? new Vector3f(0F, 1F, 0F) : new Vector3f(1F, 0F, 0F);
        Vector3f right = reference.cross(normal).normalize(size);
        Vector3f up = new Vector3f(normal).cross(right).normalize(size);

        submit(poseStack, collector, NTRenderTypes.laserFlare(), world, (last, buffer) -> {
            vertex(buffer, last, new Vector3f(middle).sub(right).sub(up), 0F, 0F, color);
            vertex(buffer, last, new Vector3f(middle).add(right).sub(up), 1F, 0F, color);
            vertex(buffer, last, new Vector3f(middle).add(right).add(up), 1F, 1F, color);
            vertex(buffer, last, new Vector3f(middle).sub(right).add(up), 0F, 1F, color);
        });
    }

    private static Vector3f cameraInPose(Matrix4f pose) {
        return new Matrix4f(pose).invert().transformPosition(new Vector3f());
    }

    private static Vector3f side(Vector3f axis, Vector3fc point, Vector3f camera, float width) {
        Vector3f side = new Vector3f(axis).cross(new Vector3f(camera).sub(point));
        if (side.lengthSquared() < MIN_LENGTH * MIN_LENGTH) {
            side = Math.abs(axis.y) < 0.99F ? new Vector3f(axis).cross(0F, 1F, 0F) : new Vector3f(axis).cross(1F, 0F, 0F);
        }
        return side.normalize(width);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Vector3f position, float u, float v, int color) {
        buffer.addVertex(pose, position.x, position.y, position.z)
                .setUv(u, v)
                .setColor(color);
    }

    private static void submit(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, boolean world,
                               SubmitNodeCollector.CustomGeometryRenderer renderer) {
        if (world) {
            ShaderPackOverlay.submit(poseStack, collector, renderType, renderer);
        } else {
            collector.submitCustomGeometry(poseStack, renderType, renderer);
        }
    }
}
