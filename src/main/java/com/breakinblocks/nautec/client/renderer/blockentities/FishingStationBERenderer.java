package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.breakinblocks.nautec.client.model.block.FishingNetModel;
import com.breakinblocks.nautec.content.blockentities.FishingStationBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FishingStationBERenderer extends NTBERenderer<FishingStationBlockEntity, FishingStationBERenderer.FishingStationRenderState> {
    private final FishingNetModel model;

    public FishingStationBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new FishingNetModel(ctx.bakeLayer(FishingNetModel.LAYER_LOCATION));
        this.model.setupAnim();
    }

    @Override
    public FishingStationRenderState createRenderState() {
        return new FishingStationRenderState();
    }

    @Override
    public void extractRenderState(FishingStationBlockEntity blockEntity, FishingStationRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(blockEntity, state);
        state.running = blockEntity.isRunning();
        state.angle = blockEntity.getIndependentAngle(partialTick);
    }

    @Override
    public void submit(FishingStationRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        poseStack.pushPose();
        {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.translate(1.75, -0.125, 0);
            poseStack.pushPose();
            {
                if (state.running) {
                    poseStack.translate(-1.75, 0, 0);
                    poseStack.mulPose(Axis.YN.rotationDegrees(state.angle));
                    poseStack.translate(1.75, 0, 0);
                }
                this.model.submit(poseStack, buffers, FishingNetModel.RENDER_TYPE, state.lightCoords, OverlayTexture.NO_OVERLAY);
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static class FishingStationRenderState extends BERenderState {
        public boolean running;
        public float angle;
    }
}
