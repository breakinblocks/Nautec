package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.breakinblocks.nautec.client.render.ShaderPackOverlay;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionControllerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionStructure;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FusionControllerBERenderer extends NTBERenderer<FusionControllerBlockEntity, FusionControllerBERenderer.FusionRenderState> {
    private static final int SEGMENTS = 36;
    private static final float COLUMN_HALF_HEIGHT = 3.0F;
    private static final float INSET = 0.03F;

    public FusionControllerBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public FusionRenderState createRenderState() {
        return new FusionRenderState();
    }

    @Override
    public void extractRenderState(FusionControllerBlockEntity controller, FusionRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(controller, state, crumbling);
        BlockPos core = controller.getVisualCore();
        state.formed = controller.isVisualFormed() && core != null;
        if (!state.formed || controller.getLevel() == null) {
            return;
        }
        BlockPos pos = controller.getBlockPos();
        state.dx = core.getX() - pos.getX();
        state.dy = core.getY() - pos.getY();
        state.dz = core.getZ() - pos.getZ();
        state.radius = controller.getVisualRadius();
        state.heat = controller.getVisualHeat();
        state.power = controller.getVisualPower();
        state.ticks = (float) (controller.getLevel().getGameTime() % 72_000L) + partialTick;
    }

    @Override
    public void submit(FusionRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.formed || state.heat <= 0F) {
            return;
        }
        float running = state.power > 0F ? 1F : 0F;
        float columnEnergy = Mth.clamp(0.12F + 0.45F * state.heat + running * (0.35F + 0.3F * state.power), 0F, 1F);
        float fieldEnergy = Mth.clamp(0.08F + 0.3F * state.heat + running * (0.25F + 0.35F * state.power), 0F, 1F);
        float half = state.radius - 0.5F - INSET;

        poseStack.pushPose();
        poseStack.translate(state.dx + 0.5, state.dy, state.dz + 0.5);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks * (1.5F + 4F * state.power)));
        cylinder(poseStack, collector, 0.95F + 0.12F * state.power, -COLUMN_HALF_HEIGHT, COLUMN_HALF_HEIGHT, color(0xFFFFFF, columnEnergy));
        if (state.radius >= 3) {
            cylinder(poseStack, collector, Math.min(half - 0.25F, 1.6F + 0.35F * state.radius), -COLUMN_HALF_HEIGHT + 0.4F, COLUMN_HALF_HEIGHT - 0.4F,
                    color(0xB8A6FF, columnEnergy * 0.55F));
        }
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.ticks * (3F + 6F * state.power)));
        disk(poseStack, collector, 0.7F, half, 0.5F, color(0xD8F6FF, Mth.clamp(columnEnergy * 0.8F, 0F, 1F)));
        poseStack.popPose();

        field(poseStack, collector, half, -COLUMN_HALF_HEIGHT + INSET, COLUMN_HALF_HEIGHT - INSET, color(0xFFFFFF, fieldEnergy));
        poseStack.popPose();
    }

    private static int color(int rgb, float alpha) {
        return ((int) (Mth.clamp(alpha, 0F, 1F) * 255F) << 24) | (rgb & 0xFFFFFF);
    }

    private static void cylinder(PoseStack poseStack, SubmitNodeCollector collector, float radius, float bottom, float top, int color) {
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.fusionPlasma(), (pose, buffer) -> {
            for (int i = 0; i < SEGMENTS; i++) {
                float u0 = i / (float) SEGMENTS;
                float u1 = (i + 1) / (float) SEGMENTS;
                float x0 = Mth.cos(u0 * Mth.TWO_PI) * radius;
                float z0 = Mth.sin(u0 * Mth.TWO_PI) * radius;
                float x1 = Mth.cos(u1 * Mth.TWO_PI) * radius;
                float z1 = Mth.sin(u1 * Mth.TWO_PI) * radius;
                vertex(buffer, pose, x0, bottom, z0, u0, 0F, color);
                vertex(buffer, pose, x1, bottom, z1, u1, 0F, color);
                vertex(buffer, pose, x1, top, z1, u1, 1F, color);
                vertex(buffer, pose, x0, top, z0, u0, 1F, color);
            }
        });
    }

    private static void disk(PoseStack poseStack, SubmitNodeCollector collector, float inner, float outer, float y, int color) {
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.fusionPlasma(), (pose, buffer) -> {
            for (int i = 0; i < SEGMENTS; i++) {
                float u0 = i / (float) SEGMENTS;
                float u1 = (i + 1) / (float) SEGMENTS;
                float c0 = Mth.cos(u0 * Mth.TWO_PI);
                float s0 = Mth.sin(u0 * Mth.TWO_PI);
                float c1 = Mth.cos(u1 * Mth.TWO_PI);
                float s1 = Mth.sin(u1 * Mth.TWO_PI);
                vertex(buffer, pose, c0 * inner, y, s0 * inner, u0, 0F, color);
                vertex(buffer, pose, c1 * inner, y, s1 * inner, u1, 0F, color);
                vertex(buffer, pose, c1 * outer, y, s1 * outer, u1, 1F, color);
                vertex(buffer, pose, c0 * outer, y, s0 * outer, u0, 1F, color);
            }
        });
    }

    private static void field(PoseStack poseStack, SubmitNodeCollector collector, float half, float bottom, float top, int color) {
        float width = half * 2F;
        float height = top - bottom;
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.fusionField(), (pose, buffer) -> {
            vertex(buffer, pose, -half, bottom, -half, 0F, 0F, color);
            vertex(buffer, pose, half, bottom, -half, width, 0F, color);
            vertex(buffer, pose, half, top, -half, width, height, color);
            vertex(buffer, pose, -half, top, -half, 0F, height, color);

            vertex(buffer, pose, half, bottom, half, 0F, 0F, color);
            vertex(buffer, pose, -half, bottom, half, width, 0F, color);
            vertex(buffer, pose, -half, top, half, width, height, color);
            vertex(buffer, pose, half, top, half, 0F, height, color);

            vertex(buffer, pose, -half, bottom, half, 0F, 0F, color);
            vertex(buffer, pose, -half, bottom, -half, width, 0F, color);
            vertex(buffer, pose, -half, top, -half, width, height, color);
            vertex(buffer, pose, -half, top, half, 0F, height, color);

            vertex(buffer, pose, half, bottom, -half, 0F, 0F, color);
            vertex(buffer, pose, half, bottom, half, width, 0F, color);
            vertex(buffer, pose, half, top, half, width, height, color);
            vertex(buffer, pose, half, top, -half, 0F, height, color);

            vertex(buffer, pose, -half, top, -half, 0F, 0F, color);
            vertex(buffer, pose, half, top, -half, width, 0F, color);
            vertex(buffer, pose, half, top, half, width, width, color);
            vertex(buffer, pose, -half, top, half, 0F, width, color);

            vertex(buffer, pose, -half, bottom, half, 0F, 0F, color);
            vertex(buffer, pose, half, bottom, half, width, 0F, color);
            vertex(buffer, pose, half, bottom, -half, width, width, color);
            vertex(buffer, pose, -half, bottom, -half, 0F, width, color);
        });
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color);
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(FusionControllerBlockEntity controller) {
        BlockPos core = controller.getVisualCore();
        if (core == null) {
            return new AABB(controller.getBlockPos());
        }
        int radius = Math.max(FusionStructure.MIN_RADIUS, controller.getVisualRadius());
        return new AABB(core).inflate(radius + 1, FusionStructure.FLOOR_DROP + 1, radius + 1);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    public static class FusionRenderState extends BlockEntityRenderState {
        public boolean formed;
        public int dx;
        public int dy;
        public int dz;
        public int radius;
        public float heat;
        public float power;
        public float ticks;
    }
}
