package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.breakinblocks.nautec.content.blockentities.CrystalCradleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CrystalCradleBERenderer extends NTBERenderer<CrystalCradleBlockEntity, CrystalCradleBERenderer.CradleRenderState> {
    private static final float SEED_SCALE = 0.08F;
    private static final float CRYSTAL_BASE = 2.98F;
    private static final float HALO_LIFT = 0.01F;

    public CrystalCradleBERenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CradleRenderState createRenderState() {
        return new CradleRenderState();
    }

    @Override
    public void extractRenderState(CrystalCradleBlockEntity cradle, CradleRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(cradle, state);
        state.seeded = cradle.hasSeed();
        state.progress = cradle.getProgress();
        long gameTime = cradle.getLevel() == null ? 0L : cradle.getLevel().getGameTime();
        state.ticks = (float) (gameTime % PrismarineCrystalRenderer.TICK_WRAP) + partialTick;
        state.seed = PrismarineCrystalRenderer.seed(cradle.getCrystalCore());
    }

    @Override
    public void submit(CradleRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        if (!state.seeded) {
            return;
        }
        float scale = SEED_SCALE + (1.0F - SEED_SCALE) * state.progress;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F + HALO_LIFT + CRYSTAL_BASE * scale, 0.5F);
        poseStack.scale(scale, scale, scale);
        PrismarineCrystalRenderer.submit(poseStack, buffers, state.ticks, state.seed, 0F, state.progress > 0.5F);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(CrystalCradleBlockEntity cradle) {
        return new AABB(cradle.getBlockPos()).expandTowards(0, 7, 0).inflate(1.5, 0, 1.5);
    }

    public static class CradleRenderState extends BERenderState {
        public boolean seeded;
        public float progress;
        public float ticks;
        public long seed;
    }
}
