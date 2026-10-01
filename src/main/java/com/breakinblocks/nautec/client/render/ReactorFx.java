package com.breakinblocks.nautec.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class ReactorFx {
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int WEST = 2;
    public static final int EAST = 3;
    public static final int UP = 4;
    public static final float TEXEL = 1.0F / 16.0F;
    public static final int WHITE = 0xFFFFFF;

    private static final Vector3f SCRATCH = new Vector3f();

    private ReactorFx() {
    }

    public static float depthOffset(double distance) {
        return 0.003F + (float) distance * 0.00012F;
    }

    public static void quad(PoseStack.Pose pose, VertexConsumer buffer, int face, float plane,
                            float u0, float u1, float v0, float v1, int bottomColor, int topColor) {
        Matrix4f matrix = pose.pose();
        vertex(matrix, buffer, face, plane, u0, v0, bottomColor);
        vertex(matrix, buffer, face, plane, u1, v0, bottomColor);
        vertex(matrix, buffer, face, plane, u1, v1, topColor);
        vertex(matrix, buffer, face, plane, u0, v1, topColor);
    }

    public static void quad(PoseStack.Pose pose, VertexConsumer buffer, int face, float plane,
                            float u0, float u1, float v0, float v1, int c00, int c10, int c11, int c01) {
        Matrix4f matrix = pose.pose();
        vertex(matrix, buffer, face, plane, u0, v0, c00);
        vertex(matrix, buffer, face, plane, u1, v0, c10);
        vertex(matrix, buffer, face, plane, u1, v1, c11);
        vertex(matrix, buffer, face, plane, u0, v1, c01);
    }

    public static float plane(float[] planes, int face, float offset) {
        return (face & 1) == 0 ? planes[face] - offset : planes[face] + offset;
    }

    public static void clippedQuad(PoseStack.Pose pose, VertexConsumer buffer, int face, float plane, float[][] rects,
                                   float u0, float u1, float v0, float v1, int bottomColor, int topColor) {
        float height = v1 - v0;
        for (float[] rect : rects) {
            float cu0 = Math.max(u0, rect[0]);
            float cu1 = Math.min(u1, rect[1]);
            float cv0 = Math.max(v0, rect[2]);
            float cv1 = Math.min(v1, rect[3]);
            if (cu0 >= cu1 || cv0 >= cv1) {
                continue;
            }
            int bottom = bottomColor;
            int top = topColor;
            if (height > 0.0F && bottomColor != topColor) {
                bottom = ARGB.linearLerp((cv0 - v0) / height, bottomColor, topColor);
                top = ARGB.linearLerp((cv1 - v0) / height, bottomColor, topColor);
            }
            quad(pose, buffer, face, plane, cu0, cu1, cv0, cv1, bottom, top);
        }
    }

    public static boolean inside(float[][] rects, float u, float v) {
        for (float[] rect : rects) {
            if (u >= rect[0] && u < rect[1] && v >= rect[2] && v < rect[3]) {
                return true;
            }
        }
        return false;
    }

    public static void pixel(PoseStack.Pose pose, VertexConsumer buffer, int face, float plane, float[][] rects,
                             float u, float v, int size, int color) {
        float su = snap(u);
        float sv = snap(v);
        float half = (size - 1) * 0.5F * TEXEL;
        clippedQuad(pose, buffer, face, plane, rects, su - half, su + TEXEL + half, sv - half, sv + TEXEL + half, color, color);
    }

    public static float snap(float value) {
        return Mth.floor(value * 16.0F) * TEXEL;
    }

    public static float churn(float u, float v, float time) {
        float a = Mth.sin(u * 2.3F + time * 0.045F + Mth.sin(v * 1.9F - time * 0.031F) * 1.4F);
        float b = Mth.sin(v * 3.1F - time * 0.052F + Mth.sin(u * 1.4F + time * 0.023F) * 1.2F);
        return a * b;
    }

    public static float hash(int seed) {
        int h = seed * 0x27D4EB2D;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        return (h & 0xFFFFFF) / (float) 0x1000000;
    }

    public static int argb(float alpha, int rgb) {
        return ARGB.color(Mth.clamp(Math.round(alpha * 255.0F), 0, 255), rgb & 0xFFFFFF);
    }

    public static int mix(int from, int to, float amount) {
        return ARGB.linearLerp(Mth.clamp(amount, 0.0F, 1.0F), from, to) & 0xFFFFFF;
    }

    public static int brighten(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        int max = Math.max(red, Math.max(green, blue));
        if (max < 24) {
            return 0xB8C8CC;
        }
        float scale = 255.0F / max;
        return (Math.min(255, Math.round(red * scale)) << 16)
                | (Math.min(255, Math.round(green * scale)) << 8)
                | Math.min(255, Math.round(blue * scale));
    }

    public static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, int color) {
        pose.pose().transformPosition(x, y, z, SCRATCH);
        buffer.addVertex(SCRATCH.x, SCRATCH.y, SCRATCH.z).setColor(color);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer buffer, int face, float plane, float u, float v, int color) {
        switch (face) {
            case NORTH, SOUTH -> matrix.transformPosition(u, v, plane, SCRATCH);
            case UP -> matrix.transformPosition(u, plane, v, SCRATCH);
            default -> matrix.transformPosition(plane, v, u, SCRATCH);
        }
        buffer.addVertex(SCRATCH.x, SCRATCH.y, SCRATCH.z).setColor(color);
    }
}
