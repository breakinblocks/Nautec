package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class BioReactorRenderer extends ReactorFxRenderer<BioReactorBlockEntity> {
    private static final float[] PLANES = {-1.0F, 2.0F, -1.0F, 2.0F};
    private static final float[] WINDOW = {0.0625F, 0.9375F, -0.5F, 0.5F};
    private static final float[][] RECTS = {WINDOW};
    private static final float MIN_U = WINDOW[0];
    private static final float MAX_U = WINDOW[1];
    private static final float BOTTOM = -0.5F;
    private static final float SURFACE = 0.25F;
    private static final float TOP = 0.5F;
    private static final float CULTURE_STEP = 0.25F;
    private static final int BUBBLES = 6;

    private int[] lattice = new int[64];

    public BioReactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected boolean shouldDraw(ReactorCultureTracker tracker) {
        return tracker.activeLevel() > 0.0F;
    }

    @Override
    protected void draw(State state, PoseStack.Pose pose, VertexConsumer buffer) {
        ReactorCultureTracker tracker = state.tracker;
        if (tracker == null) {
            return;
        }
        float time = state.time;
        float active = tracker.activeLevel();
        int tint = tint(tracker);
        float culture = active * (0.24F + 0.05F * Mth.sin(time * 0.12F));
        float peak = tracker.peakFlash();
        int flashColor = ReactorFx.mix(tracker.peakFlashColor(), ReactorFx.WHITE, 0.35F);
        int surfaceColor = ReactorFx.mix(tint, ReactorFx.WHITE, 0.35F);

        for (int face = 0; face < 4; face++) {
            float basePlane = ReactorFx.plane(PLANES, face, state.offset);
            float particlePlane = ReactorFx.plane(PLANES, face, state.offset * 1.5F);
            float beamPlane = ReactorFx.plane(PLANES, face, state.offset * 2.0F);

            if (culture > 0.004F) {
                drawCulture(pose, buffer, face, basePlane, tracker, tint, culture, time);
                ReactorFx.clippedQuad(pose, buffer, face, basePlane, RECTS, MIN_U, MAX_U, SURFACE, TOP,
                        ReactorFx.argb(culture * 0.3F, tint), ReactorFx.argb(0.0F, tint));
                drawSurface(pose, buffer, face, basePlane, surfaceColor, culture + peak * 0.5F, time);
                drawBubbles(state, pose, buffer, face, particlePlane, tint, active);
            }

            if (peak > 0.0F) {
                int colonies = tracker.colonies();
                for (int colony = 0; colony < colonies; colony++) {
                    float flash = tracker.flash(colony);
                    if (flash > 0.0F) {
                        float u = MIN_U + (colony + 0.5F) / colonies * (MAX_U - MIN_U);
                        drawBeam(pose, buffer, face, beamPlane, u, flash, ReactorFx.mix(tracker.colonyColor(colony), ReactorFx.WHITE, 0.45F));
                    }
                }
                ReactorFx.clippedQuad(pose, buffer, face, beamPlane, RECTS, MIN_U, MAX_U, BOTTOM, TOP,
                        ReactorFx.argb(peak * 0.3F, flashColor), ReactorFx.argb(peak * 0.12F, flashColor));
            }
        }
    }

    private void drawCulture(PoseStack.Pose pose, VertexConsumer buffer, int face, float plane,
                             ReactorCultureTracker tracker, int tint, float intensity, float time) {
        float faceShift = face * 5.7F;
        int columns = Math.max(1, Mth.ceil((MAX_U - MIN_U) / CULTURE_STEP));
        int rows = Math.max(1, Mth.ceil((SURFACE - BOTTOM) / CULTURE_STEP));
        float cellU = (MAX_U - MIN_U) / columns;
        float cellV = (SURFACE - BOTTOM) / rows;
        int stride = columns + 1;
        int points = stride * (rows + 1);
        if (lattice.length < points) {
            lattice = new int[points];
        }
        for (int row = 0; row <= rows; row++) {
            float v = BOTTOM + row * cellV;
            for (int column = 0; column <= columns; column++) {
                lattice[row * stride + column] = cultureColor(tracker, tint, intensity, MIN_U + column * cellU + faceShift, v, time);
            }
        }
        for (int row = 0; row < rows; row++) {
            float va = BOTTOM + row * cellV;
            float vb = BOTTOM + (row + 1) * cellV;
            for (int column = 0; column < columns; column++) {
                float ua = MIN_U + column * cellU;
                float ub = MIN_U + (column + 1) * cellU;
                int i = row * stride + column;
                ReactorFx.quad(pose, buffer, face, plane, ua, ub, va, vb,
                        lattice[i], lattice[i + 1], lattice[i + stride + 1], lattice[i + stride]);
            }
        }
    }

    private int cultureColor(ReactorCultureTracker tracker, int tint, float intensity, float u, float v, float time) {
        float churn = ReactorFx.churn(u, v, time);
        float depth = (v - BOTTOM) / (SURFACE - BOTTOM);
        float alpha = intensity * (0.62F + 0.38F * churn) * (1.0F - 0.35F * depth);
        int color = tint;
        int size = tracker.paletteSize();
        if (size > 1) {
            float swirl = ReactorFx.churn(v * 0.8F + 11.0F, u * 0.9F, time * 0.7F + 50.0F) * 0.5F + 0.5F;
            int index = Mth.clamp((int) (swirl * size), 0, size - 1);
            color = ReactorFx.mix(tint, tracker.paletteColor(index), 0.45F);
        }
        return ReactorFx.argb(alpha, color);
    }

    private void drawSurface(PoseStack.Pose pose, VertexConsumer buffer, int face, float plane, int color, float intensity, float time) {
        int column = 0;
        for (float u = ReactorFx.snap(MIN_U); u < MAX_U; u += ReactorFx.TEXEL, column++) {
            float shimmer = 0.55F + 0.45F * Mth.sin(column * 1.9F + time * 0.21F + face * 2.3F);
            int c = ReactorFx.argb(Math.min(0.9F, intensity * 1.2F * shimmer), color);
            ReactorFx.clippedQuad(pose, buffer, face, plane, RECTS, u, u + ReactorFx.TEXEL, SURFACE - ReactorFx.TEXEL, SURFACE, c, c);
        }
    }

    private void drawBubbles(State state, PoseStack.Pose pose, VertexConsumer buffer, int face, float plane, int tint, float active) {
        float minU = MIN_U + ReactorFx.TEXEL;
        float width = MAX_U - MIN_U - 3.0F * ReactorFx.TEXEL;
        float low = BOTTOM + ReactorFx.TEXEL;
        float height = SURFACE - BOTTOM - 3.0F * ReactorFx.TEXEL;
        int core = ReactorFx.mix(tint, ReactorFx.WHITE, 0.55F);
        for (int bubble = 0; bubble < BUBBLES; bubble++) {
            int seed = state.seed + face * 97 + bubble * 13;
            float period = 34.0F + ReactorFx.hash(seed) * 30.0F;
            float phase = Mth.frac(state.time / period + ReactorFx.hash(seed + 1));
            float u = minU + ReactorFx.hash(seed + 2) * width + Mth.sin(state.time * 0.09F + ReactorFx.hash(seed + 3) * Mth.TWO_PI) * ReactorFx.TEXEL * 0.8F;
            float v = low + phase * height;
            float fade = Math.min(1.0F, phase * 6.0F) * Math.min(1.0F, (1.0F - phase) * 5.0F) * active;
            ReactorFx.pixel(pose, buffer, face, plane, RECTS, u, v, 3, ReactorFx.argb(0.16F * fade, tint));
            ReactorFx.pixel(pose, buffer, face, plane, RECTS, u, v, 1, ReactorFx.argb(0.8F * fade, core));
        }
    }

    private void drawBeam(PoseStack.Pose pose, VertexConsumer buffer, int face, float plane, float u, float flash, int color) {
        float rise = Math.min(1.0F, (1.0F - Mth.sqrt(flash)) * 3.5F);
        if (rise <= 0.0F) {
            return;
        }
        float v1 = BOTTOM + (TOP - BOTTOM) * rise;
        float su = ReactorFx.snap(u);
        ReactorFx.clippedQuad(pose, buffer, face, plane, RECTS, su - ReactorFx.TEXEL, su + 2.0F * ReactorFx.TEXEL, BOTTOM, v1,
                ReactorFx.argb(flash * 0.45F, color), ReactorFx.argb(flash * 0.18F, color));
        ReactorFx.clippedQuad(pose, buffer, face, plane, RECTS, su, su + ReactorFx.TEXEL, BOTTOM, v1,
                ReactorFx.argb(flash * 0.95F, color), ReactorFx.argb(flash * 0.5F, color));
    }

    @Override
    protected AABB bounds(BioReactorBlockEntity blockEntity, ReactorCultureTracker tracker) {
        return tracker.bounds(blockEntity, -1.05D, -1.05D, -1.05D, 2.05D, 1.05D, 2.05D);
    }

    @Override
    public int getViewDistance() {
        return 48;
    }
}
