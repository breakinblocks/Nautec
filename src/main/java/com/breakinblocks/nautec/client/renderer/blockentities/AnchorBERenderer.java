package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.breakinblocks.nautec.client.model.block.AnchorModel;
import com.breakinblocks.nautec.content.blockentities.AnchorBlockEntity;
import com.breakinblocks.nautec.content.blocks.AnchorBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AnchorBERenderer extends NTBERenderer<AnchorBlockEntity, AnchorBERenderer.AnchorRenderState> {
    private final AnchorModel model;

    public AnchorBERenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new AnchorModel(context.bakeLayer(AnchorModel.LAYER_LOCATION));
        model.setupAnim();
    }

    @Override
    public AnchorRenderState createRenderState() {
        return new AnchorRenderState();
    }

    @Override
    public void extractRenderState(AnchorBlockEntity blockEntity, AnchorRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(blockEntity, state);
        state.yRot = blockEntity.getBlockState().getValue(AnchorBlock.FACING).toYRot();
    }

    @Override
    public void submit(AnchorRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        poseStack.pushPose();
        {
            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
            this.model.submit(poseStack, buffers, AnchorModel.RENDER_TYPE, state.lightCoords, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    public static class AnchorRenderState extends BERenderState {
        public float yRot;
    }
}
