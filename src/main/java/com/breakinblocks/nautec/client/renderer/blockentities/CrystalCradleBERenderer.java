package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.breakinblocks.nautec.content.blockentities.CrystalCradleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CrystalCradleBERenderer extends NTBERenderer<CrystalCradleBlockEntity, CrystalCradleBERenderer.CradleRenderState> {
    private static final float SEED_SCALE = 0.08F;
    private static final float CRYSTAL_BASE = 2.98F;

    public CrystalCradleBERenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CradleRenderState createRenderState() {
        return new CradleRenderState();
    }

    @Override
    public void extractRenderState(CrystalCradleBlockEntity cradle, CradleRenderState state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(cradle, state, crumbling);
        state.seeded = cradle.hasSeed();
        state.progress = cradle.getProgress();
        long gameTime = cradle.getLevel() == null ? 0L : cradle.getLevel().getGameTime();
        state.ticks = (float) (gameTime % PrismarineCrystalRenderer.TICK_WRAP) + partialTick;
        state.seed = PrismarineCrystalRenderer.seed(cradle.getCrystalCore());
    }

    @Override
    public void submit(CradleRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.seeded) {
            return;
        }
        float scale = SEED_SCALE + (1.0F - SEED_SCALE) * state.progress;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F + CRYSTAL_BASE * scale, 0.5F);
        poseStack.scale(scale, scale, scale);
        PrismarineCrystalRenderer.submit(poseStack, collector, state.ticks, state.seed, 0F, state.progress > 0.5F);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(CrystalCradleBlockEntity cradle) {
        return new AABB(cradle.getBlockPos()).expandTowards(0, 7, 0).inflate(1.5, 0, 1.5);
    }

    public static class CradleRenderState extends BlockEntityRenderState {
        public boolean seeded;
        public float progress;
        public float ticks;
        public long seed;
    }
}
