package com.breakinblocks.nautec.client.renderer.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserBlockEntityRenderer;
import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserRenderState;
import com.breakinblocks.nautec.client.model.block.DrainTopModel;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.content.multiblocks.DrainMultiblock;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrainBERenderer extends LaserBlockEntityRenderer<DrainBlockEntity, DrainBERenderer.DrainRenderState> {
    private final DrainTopModel model;

    public DrainBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new DrainTopModel(ctx.bakeLayer(DrainTopModel.LAYER_LOCATION));
        this.model.setupAnimation();
    }

    @Override
    public DrainRenderState createRenderState() {
        return new DrainRenderState();
    }

    @Override
    public void extractRenderState(DrainBlockEntity blockEntity, DrainRenderState state, float partialTick, Vec3 cameraPos) {
        super.extractRenderState(blockEntity, state, partialTick, cameraPos);
        state.formed = blockEntity.getBlockState().getValue(DrainMultiblock.FORMED);
        if (state.formed) {
            state.lidAngle = blockEntity.getLidIndependentAngle(partialTick);
            state.valveAngle = blockEntity.getValveIndependentAngle(partialTick);
            state.lightAbove = blockEntity.getLevel() != null
                    ? LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above())
                    : 15728880;
        }
    }

    @Override
    public void submit(DrainRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        super.submit(state, poseStack, buffers, cameraPos);
        if (state.formed) {
            poseStack.pushPose();
            {
                poseStack.translate(0, 1, 0);

                poseStack.translate(-0.75, 0, -0.75);
                poseStack.mulPose(Axis.YP.rotation(state.lidAngle));
                poseStack.translate(0.75, 0, 0.75);
                this.model.submitLid(poseStack, buffers, state.lightAbove, OverlayTexture.NO_OVERLAY);

                poseStack.translate(0.5, 0, 0.5);
                poseStack.mulPose(Axis.YP.rotation(state.valveAngle));
                poseStack.translate(-0.5, 0, -0.5);
                this.model.submitValve(poseStack, buffers, state.lightAbove, OverlayTexture.NO_OVERLAY);
            }
            poseStack.popPose();
        }
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(DrainBlockEntity blockEntity) {
        return blockEntity.getBlockState().getValue(DrainMultiblock.DRAIN_PART) == 4
                ? new AABB(blockEntity.getBlockPos()).inflate(1)
                : super.getRenderBoundingBox(blockEntity);
    }

    public static class DrainRenderState extends LaserRenderState {
        public boolean formed;
        public float lidAngle;
        public float valveAngle;
        public int lightAbove;
    }
}
