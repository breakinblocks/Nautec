package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.breakinblocks.nautec.client.render.ShaderPackOverlay;
import com.breakinblocks.nautec.client.render.SingularityLens;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalStorageBlock;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalStorageBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ExtradimensionalStorageBERenderer extends NTBERenderer<ExtradimensionalStorageBlockEntity, ExtradimensionalStorageBERenderer.SingularityRenderState> {
    private static final float[] HORIZON = {0.075F, 0.15F, 0.25F, 0.37F, 0.5F};
    private static final float[] HEIGHT = {0.72F, 1.45F, 2.1F, 2.8F, 3.5F};
    private static final float LENS_STRENGTH = 2.6F;
    private static final int SEGMENTS = 40;
    private static final int LATITUDES = 14;
    private static final float[][] RING_TILTS = {{72F, 0F}, {58F, 60F}, {64F, 120F}, {80F, 30F}};

    public ExtradimensionalStorageBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    public static float height(int tier) {
        return HEIGHT[Mth.clamp(tier, 1, HEIGHT.length) - 1];
    }

    @Override
    public SingularityRenderState createRenderState() {
        return new SingularityRenderState();
    }

    private static int tier(ExtradimensionalStorageBlockEntity storage) {
        return storage.getBlockState().hasProperty(ExtradimensionalStorageBlock.TIER)
                ? storage.getBlockState().getValue(ExtradimensionalStorageBlock.TIER) : storage.tier();
    }

    @Override
    public void extractRenderState(ExtradimensionalStorageBlockEntity storage, SingularityRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(storage, state);
        if (storage.getLevel() == null) {
            state.visible = false;
            return;
        }
        state.visible = true;
        state.tier = Mth.clamp(tier(storage), 1, ExtradimensionalStorageBlockEntity.MAX_TIER);
        state.fill = storage.fill();
        state.ticks = (float) (storage.getLevel().getGameTime() % 72_000L) + partialTick;
        state.horizon = HORIZON[state.tier - 1];
        state.height = HEIGHT[state.tier - 1];
        float glow = 0.35F + 0.65F * state.fill;
        Vec3 centre = Vec3.atLowerCornerOf(storage.getBlockPos()).add(0.5, state.height, 0.5);
        SingularityLens.submit(centre, state.horizon, LENS_STRENGTH, glow);
    }

    @Override
    public void submit(SingularityRenderState state, PoseStack poseStack, MultiBufferSource collector, Vec3 cameraPos) {
        if (!state.visible) {
            return;
        }
        float energy = 0.35F + 0.65F * state.fill;
        float pulse = 0.85F + 0.15F * Mth.sin(state.ticks * 0.08F);
        float rh = state.horizon;

        poseStack.pushPose();
        poseStack.translate(0.5, state.height, 0.5);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(14F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.ticks * (2.4F + 3F * state.fill)));
        disk(poseStack, collector, rh * 1.3F, rh * 4.6F, color(0xFFFFFF, energy * pulse));
        poseStack.popPose();

        int rings = Math.min(RING_TILTS.length, state.tier + 1);
        for (int i = 0; i < rings; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(RING_TILTS[i][1] + state.ticks * (0.6F + 0.25F * i)));
            poseStack.mulPose(Axis.XP.rotationDegrees(RING_TILTS[i][0]));
            poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks * (3F + i) * (i % 2 == 0 ? 1F : -1F)));
            float radius = rh * (5.2F + 0.55F * i);
            float band = Math.max(0.025F, rh * 0.22F);
            band(poseStack, collector, radius, band, color(0xC9A6FF, energy * 0.8F));
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks * 0.4F));
        sphere(poseStack, collector, rh * 7.2F, color(0xC8A0FF, Mth.clamp(0.35F + 0.5F * energy, 0F, 1F)));
        poseStack.popPose();

        poseStack.popPose();
    }

    private static int color(int rgb, float alpha) {
        return ((int) (Mth.clamp(alpha, 0F, 1F) * 255F) << 24) | (rgb & 0xFFFFFF);
    }

    private static void disk(PoseStack poseStack, MultiBufferSource collector, float inner, float outer, int color) {
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.singularityDisk(), (pose, buffer) -> {
            for (int i = 0; i < SEGMENTS; i++) {
                float u0 = i / (float) SEGMENTS;
                float u1 = (i + 1) / (float) SEGMENTS;
                float c0 = Mth.cos(u0 * Mth.TWO_PI);
                float s0 = Mth.sin(u0 * Mth.TWO_PI);
                float c1 = Mth.cos(u1 * Mth.TWO_PI);
                float s1 = Mth.sin(u1 * Mth.TWO_PI);
                vertex(buffer, pose, c0 * inner, 0F, s0 * inner, u0, 0F, color);
                vertex(buffer, pose, c1 * inner, 0F, s1 * inner, u1, 0F, color);
                vertex(buffer, pose, c1 * outer, 0F, s1 * outer, u1, 1F, color);
                vertex(buffer, pose, c0 * outer, 0F, s0 * outer, u0, 1F, color);
            }
        });
    }

    private static void band(PoseStack poseStack, MultiBufferSource collector, float radius, float height, int color) {
        float half = height * 0.5F;
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.fusionPlasma(), (pose, buffer) -> {
            for (int i = 0; i < SEGMENTS; i++) {
                float u0 = i / (float) SEGMENTS;
                float u1 = (i + 1) / (float) SEGMENTS;
                float x0 = Mth.cos(u0 * Mth.TWO_PI) * radius;
                float z0 = Mth.sin(u0 * Mth.TWO_PI) * radius;
                float x1 = Mth.cos(u1 * Mth.TWO_PI) * radius;
                float z1 = Mth.sin(u1 * Mth.TWO_PI) * radius;
                vertex(buffer, pose, x0, -half, z0, u0, 0F, color);
                vertex(buffer, pose, x1, -half, z1, u1, 0F, color);
                vertex(buffer, pose, x1, half, z1, u1, 1F, color);
                vertex(buffer, pose, x0, half, z0, u0, 1F, color);
            }
        });
    }

    private static void sphere(PoseStack poseStack, MultiBufferSource collector, float radius, int color) {
        float around = Mth.TWO_PI * radius;
        float span = Mth.PI * radius;
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.fusionField(), (pose, buffer) -> {
            for (int lat = 0; lat < LATITUDES; lat++) {
                float v0 = lat / (float) LATITUDES;
                float v1 = (lat + 1) / (float) LATITUDES;
                float p0 = Mth.PI * v0;
                float p1 = Mth.PI * v1;
                float y0 = Mth.cos(p0) * radius;
                float y1 = Mth.cos(p1) * radius;
                float r0 = Mth.sin(p0) * radius;
                float r1 = Mth.sin(p1) * radius;
                for (int lon = 0; lon < SEGMENTS; lon++) {
                    float u0 = lon / (float) SEGMENTS;
                    float u1 = (lon + 1) / (float) SEGMENTS;
                    float c0 = Mth.cos(u0 * Mth.TWO_PI);
                    float s0 = Mth.sin(u0 * Mth.TWO_PI);
                    float c1 = Mth.cos(u1 * Mth.TWO_PI);
                    float s1 = Mth.sin(u1 * Mth.TWO_PI);
                    vertex(buffer, pose, c0 * r0, y0, s0 * r0, u0 * around, v0 * span, color);
                    vertex(buffer, pose, c1 * r0, y0, s1 * r0, u1 * around, v0 * span, color);
                    vertex(buffer, pose, c1 * r1, y1, s1 * r1, u1 * around, v1 * span, color);
                    vertex(buffer, pose, c0 * r1, y1, s0 * r1, u0 * around, v1 * span, color);
                }
            }
        });
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color);
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(ExtradimensionalStorageBlockEntity storage) {
        int tier = Mth.clamp(tier(storage), 1, ExtradimensionalStorageBlockEntity.MAX_TIER);
        float reach = HORIZON[tier - 1] * 8F + 0.5F;
        return new AABB(storage.getBlockPos()).inflate(reach, HEIGHT[tier - 1] + reach, reach);
    }

    @Override
    public boolean shouldRenderOffScreen(ExtradimensionalStorageBlockEntity storage) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static class SingularityRenderState extends BERenderState {
        public boolean visible;
        public int tier;
        public float fill;
        public float ticks;
        public float horizon;
        public float height;
    }
}
