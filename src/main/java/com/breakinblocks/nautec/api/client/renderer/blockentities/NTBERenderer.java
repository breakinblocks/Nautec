package com.breakinblocks.nautec.api.client.renderer.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class NTBERenderer<T extends BlockEntity, S extends BERenderState> implements BlockEntityRenderer<T> {
    protected final BlockEntityRendererProvider.Context context;

    public NTBERenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    public abstract S createRenderState();

    public void extractRenderState(T blockEntity, S state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(blockEntity, state);
    }

    public abstract void submit(S state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos);

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Vec3 cameraPos = this.context.getBlockEntityRenderDispatcher().camera.getPosition();
        S state = createRenderState();
        extractRenderState(blockEntity, state, partialTick, cameraPos);
        state.lightCoords = packedLight;
        state.overlayCoords = packedOverlay;
        submit(state, poseStack, buffers, cameraPos);
    }
}
