package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.breakinblocks.nautec.content.resonance.ResonancePylonBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResonancePylonBERenderer extends NTBERenderer<ResonancePylonBlockEntity, ResonancePylonBERenderer.PylonRenderState> {
    private static final float BASIC_SCALE = 0.11F;
    private static final float ABYSSAL_SCALE = 0.14F;
    private static final float HOVER = 0.7F;

    public ResonancePylonBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public PylonRenderState createRenderState() {
        return new PylonRenderState();
    }

    @Override
    public void extractRenderState(ResonancePylonBlockEntity pylon, PylonRenderState state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(pylon, state, crumbling);
        state.active = pylon.isVisualActive();
        state.linked = pylon.getNetworkId() != null;
        state.interdimensional = pylon.interdimensional();
        state.ticks = pylon.getLevel() == null ? 0F : (float) (pylon.getLevel().getGameTime() % PrismarineCrystalRenderer.TICK_WRAP) + partialTick;
        state.seed = PrismarineCrystalRenderer.seed(pylon.getBlockPos());
    }

    @Override
    public void submit(PylonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float scale = state.interdimensional ? ABYSSAL_SCALE : BASIC_SCALE;
        float speed = state.active ? 3F : state.linked ? 1.2F : 0.5F;
        float flash = state.active ? 0.45F + 0.25F * Mth.sin(state.ticks * 0.3F) : state.linked ? 0.1F : 0F;
        poseStack.pushPose();
        poseStack.translate(0.5F, HOVER + Mth.sin(state.ticks * 0.05F) * 0.04F, 0.5F);
        poseStack.scale(scale, scale, scale);
        PrismarineCrystalRenderer.submit(poseStack, collector, state.ticks * speed, state.seed, flash, true);
        poseStack.popPose();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(ResonancePylonBlockEntity pylon) {
        return new AABB(pylon.getBlockPos()).expandTowards(0, 1.5, 0);
    }

    public static class PylonRenderState extends BlockEntityRenderState {
        public boolean active;
        public boolean linked;
        public boolean interdimensional;
        public float ticks;
        public long seed;
    }
}
