package com.breakinblocks.nautec.client.renderer.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserBlockEntityRenderer;
import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserRenderState;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrismarineCrystalBERenderer extends LaserBlockEntityRenderer<PrismarineCrystalBlockEntity, PrismarineCrystalBERenderer.PrismarineCrystalRenderState> {
    public PrismarineCrystalBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public PrismarineCrystalRenderState createRenderState() {
        return new PrismarineCrystalRenderState();
    }

    @Override
    public void extractRenderState(PrismarineCrystalBlockEntity blockEntity, PrismarineCrystalRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumbling);
        state.breaking = blockEntity.isBreaking();
        state.breakingProgress = 0;
        if (state.breaking && blockEntity.getLevel() != null) {
            state.breakingProgress = ((float) (blockEntity.getLevel().getGameTime() - blockEntity.getStartTick()) + partialTick) / (float) blockEntity.getDuration();
        }
        state.ticks = (float) (state.gameTime % PrismarineCrystalRenderer.TICK_WRAP) + partialTick;
        state.seed = PrismarineCrystalRenderer.seed(blockEntity.getBlockPos());
    }

    @Override
    public void submit(PrismarineCrystalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        poseStack.pushPose();
        {
            poseStack.translate(0.5, 0, 0.5);

            float flash = 0F;
            if (state.breaking) {
                float f = state.breakingProgress;
                if (f >= 0.0F && f <= 1.0F) {
                    float f2 = f * 6.2831855F;
                    float f3 = -1.5F * (Mth.cos(f2) + 0.5F) * Mth.sin(f2 / 2.0F);
                    poseStack.mulPose(Axis.XP.rotation(f3 * 0.015625F));
                    float f4 = Mth.sin(f2);
                    poseStack.mulPose(Axis.ZP.rotation(f4 * 0.015625F));
                    flash = Mth.sin(f * Mth.PI) * 0.6F;
                }
            }
            PrismarineCrystalRenderer.submit(poseStack, collector, state.ticks, state.seed, flash, true);
        }
        poseStack.popPose();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(PrismarineCrystalBlockEntity blockEntity) {
        AABB crystal = new AABB(blockEntity.getBlockPos().below(3)).expandTowards(0, 5, 0).inflate(1.5, 0, 1.5);
        return blockEntity.shouldRender(Direction.UP) ? crystal.minmax(super.getRenderBoundingBox(blockEntity)) : crystal;
    }

    public static class PrismarineCrystalRenderState extends LaserRenderState {
        public boolean breaking;
        public float breakingProgress;
        public float ticks;
        public long seed;
    }
}
