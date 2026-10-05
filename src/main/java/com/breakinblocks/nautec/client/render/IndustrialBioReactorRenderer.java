package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.content.blockentities.multiblock.controller.IndustrialBioReactorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class IndustrialBioReactorRenderer extends ReactorFxRenderer<IndustrialBioReactorBlockEntity> {
    private static final float CHAMBER_OFFSET = 2.0F;
    private static final float HALF = 1.5F;
    private static final float FLOOR = 0.0F;
    private static final float SURFACE = 1.5F;
    private static final float CEILING = 2.0F;
    private static final float INSET = 0.03F;
    private static final float CELL = 0.5F;
    private static final float[] SHELL_SCALES = {1.0F, 0.66F, 0.34F};
    private static final float[] SHELL_WEIGHTS = {0.5F, 0.4F, 0.35F};
    private static final float SURFACE_WEIGHT = 0.6F;
    private static final int MOTES = 16;
    private static final float MOTE_CORE = 0.035F;
    private static final float MOTE_HALO = 0.11F;
    private static final float BEAM_CORE = 0.04F;
    private static final float BEAM_HALO = 0.14F;
    private static final float IDLE_CULTURE = 0.1F;
    private static final float ACTIVE_CULTURE = 0.27F;

    private int[] lattice = new int[64];
    private float centerX;
    private float centerZ;
    private float rightX;
    private float rightZ;
    private float upX;
    private float upY;
    private float upZ;

    public IndustrialBioReactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected boolean shouldDraw(ReactorCultureTracker tracker) {
        return tracker.paletteSize() > 0;
    }

    @Override
    protected void draw(State state, PoseStack.Pose pose, VertexConsumer buffer) {
        ReactorCultureTracker tracker = state.tracker;
        if (tracker == null) {
            return;
        }
        float time = state.time;
        centerX = chamberCenterX(state.front);
        centerZ = chamberCenterZ(state.front);
        float active = tracker.activeLevel();
        float peak = tracker.peakFlash();
        int tint = ReactorFx.mix(tint(tracker), tracker.peakFlashColor(), peak * 0.5F);
        float intensity = 0.0F;
        if (tracker.paletteSize() > 0) {
            intensity = Mth.lerp(active, IDLE_CULTURE, ACTIVE_CULTURE) + active * 0.04F * Mth.sin(time * 0.1F);
        }
        intensity += peak * 0.3F;

        orient(state.cameraX - centerX, state.cameraY - SURFACE * 0.5F, state.cameraZ - centerZ);

        if (intensity > 0.004F) {
            for (int shell = 0; shell < SHELL_SCALES.length; shell++) {
                drawShell(pose, buffer, tracker, tint, intensity * SHELL_WEIGHTS[shell], SHELL_SCALES[shell], shell == 0, time);
            }
            drawWaterline(pose, buffer, ReactorFx.mix(tint, ReactorFx.WHITE, 0.35F), intensity, time);
        }

        if (tracker.paletteSize() > 0) {
            drawMotes(state, pose, buffer, tracker, tint, active);
        }

        if (peak > 0.0F) {
            for (int colony = 0; colony < tracker.colonies(); colony++) {
                float flash = tracker.flash(colony);
                if (flash > 0.0F) {
                    drawColonyFlash(state, pose, buffer, colony, flash, ReactorFx.mix(tracker.colonyColor(colony), ReactorFx.WHITE, 0.45F));
                }
            }
        }
    }

    private void orient(float dx, float dy, float dz) {
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-3F) {
            dx = 0.0F;
            dy = 0.0F;
            dz = 1.0F;
        } else {
            dx /= length;
            dy /= length;
            dz /= length;
        }
        float horizontal = Mth.sqrt(dx * dx + dz * dz);
        if (horizontal < 1.0E-3F) {
            this.rightX = 1.0F;
            this.rightZ = 0.0F;
        } else {
            this.rightX = dz / horizontal;
            this.rightZ = -dx / horizontal;
        }
        this.upX = dy * this.rightZ;
        this.upY = dz * this.rightX - dx * this.rightZ;
        this.upZ = -dy * this.rightX;
    }

    private void drawShell(PoseStack.Pose pose, VertexConsumer buffer, ReactorCultureTracker tracker, int tint,
                           float intensity, float scale, boolean outer, float time) {
        float half = (HALF - INSET) * scale;
        float x0 = centerX - half;
        float x1 = centerX + half;
        float z0 = centerZ - half;
        float z1 = centerZ + half;
        float y0 = FLOOR + INSET;
        float y1 = outer ? SURFACE : y0 + (SURFACE - y0) * (0.45F + 0.55F * scale);
        drawGrid(pose, buffer, tracker, tint, intensity, time, ReactorFx.NORTH, z0, x0, x1, y0, y1);
        drawGrid(pose, buffer, tracker, tint, intensity, time, ReactorFx.SOUTH, z1, x0, x1, y0, y1);
        drawGrid(pose, buffer, tracker, tint, intensity, time, ReactorFx.WEST, x0, z0, z1, y0, y1);
        drawGrid(pose, buffer, tracker, tint, intensity, time, ReactorFx.EAST, x1, z0, z1, y0, y1);
        drawGrid(pose, buffer, tracker, tint, outer ? intensity * SURFACE_WEIGHT / SHELL_WEIGHTS[0] : intensity, time, ReactorFx.UP, y1, x0, x1, z0, z1);
    }

    private void drawGrid(PoseStack.Pose pose, VertexConsumer buffer, ReactorCultureTracker tracker, int tint, float intensity,
                          float time, int face, float plane, float u0, float u1, float v0, float v1) {
        int columns = Math.max(1, Mth.ceil((u1 - u0) / CELL));
        int rows = Math.max(1, Mth.ceil((v1 - v0) / CELL));
        float cellU = (u1 - u0) / columns;
        float cellV = (v1 - v0) / rows;
        int stride = columns + 1;
        int points = stride * (rows + 1);
        if (lattice.length < points) {
            lattice = new int[points];
        }
        for (int row = 0; row <= rows; row++) {
            float v = v0 + row * cellV;
            for (int column = 0; column <= columns; column++) {
                lattice[row * stride + column] = gridColor(tracker, tint, intensity, time, face, plane, u0 + column * cellU, v);
            }
        }
        for (int row = 0; row < rows; row++) {
            float va = v0 + row * cellV;
            float vb = v0 + (row + 1) * cellV;
            for (int column = 0; column < columns; column++) {
                float ua = u0 + column * cellU;
                float ub = u0 + (column + 1) * cellU;
                int i = row * stride + column;
                ReactorFx.quad(pose, buffer, face, plane, ua, ub, va, vb,
                        lattice[i], lattice[i + 1], lattice[i + stride + 1], lattice[i + stride]);
            }
        }
    }

    private int gridColor(ReactorCultureTracker tracker, int tint, float intensity, float time, int face, float plane, float u, float v) {
        float x;
        float y;
        float z;
        switch (face) {
            case ReactorFx.NORTH, ReactorFx.SOUTH -> {
                x = u;
                y = v;
                z = plane;
            }
            case ReactorFx.UP -> {
                x = u;
                y = plane;
                z = v;
            }
            default -> {
                x = plane;
                y = v;
                z = u;
            }
        }
        return cultureColor(tracker, tint, intensity, x, y, z, time);
    }

    private int cultureColor(ReactorCultureTracker tracker, int tint, float intensity, float x, float y, float z, float time) {
        float churn = ReactorFx.churn(x + 0.43F * z, y + 0.29F * z, time) * 0.6F
                + ReactorFx.churn(z - 0.31F * x + 7.0F, y * 1.2F, time * 1.13F + 20.0F) * 0.4F;
        float alpha = intensity * (0.6F + 0.4F * churn) * (1.0F - 0.3F * (y / SURFACE));
        int color = tint;
        int size = tracker.paletteSize();
        if (size > 1) {
            float swirl = ReactorFx.churn(z * 0.8F + 0.3F * x + 11.0F, x * 0.9F + 0.5F * y, time * 0.7F + 50.0F) * 0.5F + 0.5F;
            int index = Mth.clamp((int) (swirl * size), 0, size - 1);
            color = ReactorFx.mix(tint, tracker.paletteColor(index), 0.45F);
        }
        return ReactorFx.argb(alpha, color);
    }

    private void drawWaterline(PoseStack.Pose pose, VertexConsumer buffer, int color, float intensity, float time) {
        float inset = INSET * 0.5F;
        float x0 = centerX - HALF + inset;
        float x1 = centerX + HALF - inset;
        float z0 = centerZ - HALF + inset;
        float z1 = centerZ + HALF - inset;
        drawWaterlineWall(pose, buffer, ReactorFx.NORTH, z0, x0, x1, color, intensity, time);
        drawWaterlineWall(pose, buffer, ReactorFx.SOUTH, z1, x0, x1, color, intensity, time);
        drawWaterlineWall(pose, buffer, ReactorFx.WEST, x0, z0, z1, color, intensity, time);
        drawWaterlineWall(pose, buffer, ReactorFx.EAST, x1, z0, z1, color, intensity, time);
    }

    private void drawWaterlineWall(PoseStack.Pose pose, VertexConsumer buffer, int face, float plane, float u0, float u1,
                                   int color, float intensity, float time) {
        int column = 0;
        for (float u = ReactorFx.snap(u0); u < u1; u += ReactorFx.TEXEL, column++) {
            float shimmer = 0.55F + 0.45F * Mth.sin(column * 1.9F + time * 0.21F + face * 2.3F);
            int c = ReactorFx.argb(Math.min(0.9F, intensity * 1.2F * shimmer), color);
            int fade = ReactorFx.argb(0.0F, color);
            float ua = Math.max(u, u0);
            float ub = Math.min(u + ReactorFx.TEXEL, u1);
            ReactorFx.quad(pose, buffer, face, plane, ua, ub, SURFACE - ReactorFx.TEXEL, SURFACE, c, c);
            ReactorFx.quad(pose, buffer, face, plane, ua, ub, SURFACE, SURFACE + 0.25F, ReactorFx.argb(intensity * 0.25F * shimmer, color), fade);
        }
    }

    private void drawMotes(State state, PoseStack.Pose pose, VertexConsumer buffer, ReactorCultureTracker tracker, int tint, float active) {
        float reach = HALF - 0.15F;
        float low = FLOOR + 0.1F;
        float height = SURFACE - 0.2F;
        float brightness = 0.25F + 0.6F * active;
        int size = tracker.paletteSize();
        for (int mote = 0; mote < MOTES; mote++) {
            int seed = state.seed + mote * 17;
            float speed = 0.006F + ReactorFx.hash(seed + 2) * 0.012F;
            float x = centerX + reach * Mth.sin(state.time * speed + ReactorFx.hash(seed) * Mth.TWO_PI);
            float z = centerZ + reach * Mth.sin(state.time * speed * 1.21F + ReactorFx.hash(seed + 1) * Mth.TWO_PI);
            float y = low + height * (0.5F + 0.5F * Mth.sin(state.time * speed * 0.83F + ReactorFx.hash(seed + 4) * Mth.TWO_PI));
            float twinkle = 0.6F + 0.4F * Mth.sin(state.time * 0.2F + ReactorFx.hash(seed + 3) * Mth.TWO_PI);
            float alpha = brightness * twinkle;
            int color = ReactorFx.mix(tracker.paletteColor(mote % size), ReactorFx.WHITE, 0.5F);
            billboard(pose, buffer, x, y, z, MOTE_HALO, ReactorFx.argb(alpha * 0.2F, tint));
            billboard(pose, buffer, x, y, z, MOTE_CORE, ReactorFx.argb(alpha, color));
        }
    }

    private void billboard(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float size, int color) {
        float rx = this.rightX * size;
        float rz = this.rightZ * size;
        float ux = this.upX * size;
        float uy = this.upY * size;
        float uz = this.upZ * size;
        ReactorFx.vertex(pose, buffer, x - rx - ux, y - uy, z - rz - uz, color);
        ReactorFx.vertex(pose, buffer, x + rx - ux, y - uy, z + rz - uz, color);
        ReactorFx.vertex(pose, buffer, x + rx + ux, y + uy, z + rz + uz, color);
        ReactorFx.vertex(pose, buffer, x - rx + ux, y + uy, z - rz + uz, color);
    }

    private void drawColonyFlash(State state, PoseStack.Pose pose, VertexConsumer buffer, int colony, float flash, int color) {
        float cx = centerX - HALF + colony % 3 + 0.5F;
        float cz = centerZ - HALF + colony / 3 + 0.5F;
        float spread = 1.0F - Mth.sqrt(flash);
        float rise = Math.min(1.0F, spread * 3.5F);

        float floorHalf = 0.4F;
        int floorColor = ReactorFx.argb(flash * 0.45F, color);
        ReactorFx.quad(pose, buffer, ReactorFx.UP, FLOOR + INSET + 0.005F, cx - floorHalf, cx + floorHalf, cz - floorHalf, cz + floorHalf,
                floorColor, floorColor);

        float ripple = 0.15F + 0.33F * spread;
        int rippleColor = ReactorFx.argb(flash * 0.35F, color);
        ReactorFx.quad(pose, buffer, ReactorFx.UP, SURFACE + 0.005F, cx - ripple, cx + ripple, cz - ripple, cz + ripple,
                rippleColor, rippleColor);

        if (rise <= 0.0F) {
            return;
        }
        float dx = state.cameraX - cx;
        float dz = state.cameraZ - cz;
        float horizontal = Mth.sqrt(dx * dx + dz * dz);
        float sx = horizontal < 1.0E-3F ? 1.0F : dz / horizontal;
        float sz = horizontal < 1.0E-3F ? 0.0F : -dx / horizontal;
        float y0 = FLOOR + INSET;
        float y1 = y0 + (CEILING - 2.0F * INSET - y0) * rise;
        column(pose, buffer, cx, cz, sx, sz, BEAM_HALO, y0, y1, ReactorFx.argb(flash * 0.45F, color), ReactorFx.argb(flash * 0.12F, color));
        column(pose, buffer, cx, cz, sx, sz, BEAM_CORE, y0, y1, ReactorFx.argb(flash * 0.95F, color), ReactorFx.argb(flash * 0.4F, color));
    }

    private void column(PoseStack.Pose pose, VertexConsumer buffer, float cx, float cz, float sx, float sz, float half,
                        float y0, float y1, int bottomColor, int topColor) {
        float ox = sx * half;
        float oz = sz * half;
        ReactorFx.vertex(pose, buffer, cx - ox, y0, cz - oz, bottomColor);
        ReactorFx.vertex(pose, buffer, cx + ox, y0, cz + oz, bottomColor);
        ReactorFx.vertex(pose, buffer, cx + ox, y1, cz + oz, topColor);
        ReactorFx.vertex(pose, buffer, cx - ox, y1, cz - oz, topColor);
    }

    @Override
    protected AABB bounds(IndustrialBioReactorBlockEntity blockEntity, ReactorCultureTracker tracker) {
        Direction front = blockEntity.front();
        double x = chamberCenterX(front);
        double z = chamberCenterZ(front);
        return tracker.bounds(blockEntity, x - 1.55D, -0.05D, z - 1.55D, x + 1.55D, 2.05D, z + 1.55D);
    }

    private static float chamberCenterX(Direction front) {
        return 0.5F - front.getStepX() * CHAMBER_OFFSET;
    }

    private static float chamberCenterZ(Direction front) {
        return 0.5F - front.getStepZ() * CHAMBER_OFFSET;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
