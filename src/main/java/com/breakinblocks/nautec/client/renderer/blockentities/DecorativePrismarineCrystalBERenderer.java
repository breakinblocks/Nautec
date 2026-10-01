package com.breakinblocks.nautec.client.renderer.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.breakinblocks.nautec.content.blockentities.DecorativePrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blocks.DecorativePrismarineCrystalBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DecorativePrismarineCrystalBERenderer extends NTBERenderer<DecorativePrismarineCrystalBlockEntity, DecorativePrismarineCrystalBERenderer.DecorativeCrystalRenderState> {
    public DecorativePrismarineCrystalBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public DecorativeCrystalRenderState createRenderState() {
        return new DecorativeCrystalRenderState();
    }

    @Override
    public void extractRenderState(DecorativePrismarineCrystalBlockEntity blockEntity, DecorativeCrystalRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        long gameTime = blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime();
        state.ticks = (float) (gameTime % PrismarineCrystalRenderer.TICK_WRAP) + partialTick;
        state.seed = PrismarineCrystalRenderer.seed(blockEntity.getBlockPos());
    }

    @Override
    public void submit(DecorativeCrystalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        {
            poseStack.translate(0.5, DecorativePrismarineCrystalBlock.HEIGHT / 2.0, 0.5);
            PrismarineCrystalRenderer.submit(poseStack, collector, state.ticks, state.seed, 0F, true);
        }
        poseStack.popPose();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(DecorativePrismarineCrystalBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos())
                .expandTowards(0, DecorativePrismarineCrystalBlock.HEIGHT - 1, 0)
                .inflate(1.5, 0, 1.5);
    }

    public static class DecorativeCrystalRenderState extends BlockEntityRenderState {
        public float ticks;
        public long seed;
    }
}
